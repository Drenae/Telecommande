@file:Suppress("DEPRECATION")

package com.telecommande.core.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.telecommande.core.event.DiscoveryEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import java.util.ArrayDeque
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class TvDiscoveryManager(context: Context) {
    private val appContext: Context = context.applicationContext
    private val nsdManager: NsdManager? = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Erreur non interceptée dans TvDiscoveryManager")
        }
    )

    private var currentDiscoveryListener: NsdManager.DiscoveryListener? = null

    // API <= 33 : resolveService() ne doit pas être lancé en parallèle de manière agressive.
    // Les TV trouvées sont donc résolues séquentiellement via cette file.
    private val legacyResolveQueue = ArrayDeque<NsdServiceInfo>()
    private val legacyResolveQueueLock = Any()
    private var activeLegacyResolveServiceName: String? = null
    private val activeResolveListeners = ConcurrentHashMap<String, NsdManager.ResolveListener>()
    private val resolveRetryCounts = ConcurrentHashMap<String, Int>()

    // API 34+ : chaque service découvert est suivi avec ServiceInfoCallback, API recommandée
    // à la place de resolveService() qui est dépréciée depuis Android 14.
    private val serviceInfoCallbacks = ConcurrentHashMap<String, NsdManager.ServiceInfoCallback>()
    private val serviceInfoRetryCounts = ConcurrentHashMap<String, Int>()

    private var multicastLock: WifiManager.MulticastLock? = null
    private val nsdExecutor: Executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "NsdResolveThread").apply { isDaemon = true }
    }

    private val servicesToResolve = Collections.synchronizedSet(HashSet<String>())
    private val resolvedServicesCache = ConcurrentHashMap<String, DiscoveredTv>()
    private val discoveryLock = Mutex()

    private val _eventFlow = MutableSharedFlow<DiscoveryEvent>(replay = 0, extraBufferCapacity = 16)
    val eventFlow: Flow<DiscoveryEvent> = _eventFlow.asSharedFlow()

    companion object {
        const val SERVICE_TYPE_ANDROID_TV_REMOTE = "_androidtvremote2._tcp."

        private const val MAX_RESOLVE_RETRIES = 3
        private const val RESOLVE_RETRY_DELAY_MS = 300L
    }

    init {
        if (nsdManager == null) {
            Timber.e("NsdManager n'est pas disponible. La découverte de services ne fonctionnera pas.")
            coroutineScope.launch {
                _eventFlow.tryEmit(DiscoveryEvent.Error("NsdManager non disponible"))
            }
        }
    }

    fun startDiscovery() {
        coroutineScope.launch {
            discoveryLock.withLock {
                if (nsdManager == null) {
                    Timber.w("NsdManager non disponible, impossible de démarrer la découverte.")
                    _eventFlow.tryEmit(DiscoveryEvent.Error("NsdManager non disponible"))
                    return@launch
                }

                if (currentDiscoveryListener != null) {
                    Timber.w("La découverte est déjà active. Arrêt propre avant redémarrage.")
                    stopDiscoveryInternal(notifyListener = false, releaseLock = false)
                }

                Timber.i(
                    "Démarrage de la découverte de TV pour le type de service : %s",
                    SERVICE_TYPE_ANDROID_TV_REMOTE
                )
                acquireMulticastLock()
                resetResolutionState(emitLost = true)

                val listener = initializeNsdDiscoveryListener()
                currentDiscoveryListener = listener

                try {
                    nsdManager.discoverServices(
                        SERVICE_TYPE_ANDROID_TV_REMOTE,
                        NsdManager.PROTOCOL_DNS_SD,
                        listener
                    )
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "Erreur au démarrage de la découverte NSD")
                    _eventFlow.tryEmit(
                        DiscoveryEvent.Error(
                            "Échec du démarrage de la découverte: ${e.message}",
                            0
                        )
                    )
                    currentDiscoveryListener = null
                    releaseMulticastLock()
                }
            }
        }
    }

    fun stopDiscovery() {
        coroutineScope.launch {
            discoveryLock.withLock {
                stopDiscoveryInternal(notifyListener = true, releaseLock = true)
            }
        }
    }

    private fun stopDiscoveryInternal(notifyListener: Boolean, releaseLock: Boolean) {
        Timber.i("Arrêt de la découverte de TV (notify: %s).", notifyListener)

        currentDiscoveryListener?.let { listener ->
            try {
                nsdManager?.stopServiceDiscovery(listener)
            } catch (e: Exception) {
                Timber.w(e, "Erreur lors de l'arrêt de la découverte NSD.")
            } finally {
                currentDiscoveryListener = null
            }
        }

        unregisterAllServiceInfoCallbacks()
        clearLegacyResolveState()
        servicesToResolve.clear()
        resolveRetryCounts.clear()
        serviceInfoRetryCounts.clear()

        if (releaseLock) {
            releaseMulticastLock()
        }

        if (notifyListener) {
            _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStopped)
        }
    }

    private fun resetResolutionState(emitLost: Boolean) {
        unregisterAllServiceInfoCallbacks()
        clearLegacyResolveState()
        servicesToResolve.clear()
        resolveRetryCounts.clear()
        serviceInfoRetryCounts.clear()

        if (emitLost) {
            resolvedServicesCache.forEach { (_, tv) ->
                _eventFlow.tryEmit(DiscoveryEvent.TvLost(tv))
            }
        }
        resolvedServicesCache.clear()
    }

    private fun clearLegacyResolveState() {
        synchronized(legacyResolveQueueLock) {
            legacyResolveQueue.clear()
            activeLegacyResolveServiceName = null
        }
        activeResolveListeners.clear()
    }

    private fun initializeNsdDiscoveryListener(): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Timber.d("Découverte NSD démarrée : %s", regType)
                _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStarted)
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Timber.i(
                    "Service NSD trouvé : Nom=%s, Type=%s",
                    service.serviceName,
                    service.serviceType
                )

                var currentServiceType = service.serviceType
                if (currentServiceType == null) {
                    Timber.w("Service trouvé avec un type nul. Ignoré.")
                    return
                }
                if (!currentServiceType.endsWith(".")) {
                    currentServiceType += "."
                }

                if (!SERVICE_TYPE_ANDROID_TV_REMOTE.equals(currentServiceType, ignoreCase = true)) {
                    Timber.d(
                        "Ignorance du service avec le type : %s (Attendu : %s)",
                        service.serviceType,
                        SERVICE_TYPE_ANDROID_TV_REMOTE
                    )
                    return
                }

                val serviceName = service.serviceName ?: run {
                    Timber.w("Service trouvé avec un nom nul. Ignoré.")
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    registerModernServiceInfoCallback(service)
                } else {
                    enqueueLegacyResolve(service)
                }

                Timber.d("Service %s pris en charge pour résolution/suivi.", serviceName)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                val serviceName = service.serviceName ?: "Inconnu"
                Timber.i("Service NSD perdu : %s", serviceName)

                servicesToResolve.remove(serviceName)
                removeFromLegacyResolveQueue(serviceName)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    unregisterServiceInfoCallback(serviceName)
                }

                emitServiceLost(serviceName)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Timber.d("Découverte NSD explicitement arrêtée pour : %s", serviceType)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Timber.e(
                    "Échec du démarrage de la découverte NSD : Type=%s, CodeErreur=%d",
                    serviceType,
                    errorCode
                )
                _eventFlow.tryEmit(
                    DiscoveryEvent.Error("Échec du démarrage de la découverte.", errorCode)
                )
                coroutineScope.launch {
                    discoveryLock.withLock {
                        stopDiscoveryInternal(notifyListener = false, releaseLock = true)
                    }
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Timber.e(
                    "Échec de l'arrêt de la découverte NSD : Type=%s, CodeErreur=%d",
                    serviceType,
                    errorCode
                )
                _eventFlow.tryEmit(
                    DiscoveryEvent.Error("Échec de l'arrêt de la découverte.", errorCode)
                )
                releaseMulticastLock()
            }
        }
    }

    /**
     * Android 14+ : suit le service avec ServiceInfoCallback. Cela évite de lancer plusieurs
     * resolveService() concurrents et permet de recevoir une nouvelle adresse si elle change.
     */
    private fun registerModernServiceInfoCallback(service: NsdServiceInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return

        val serviceName = service.serviceName ?: return
        if (serviceInfoCallbacks.containsKey(serviceName)) {
            Timber.d("Callback ServiceInfo déjà actif pour %s.", serviceName)
            return
        }

        val callback = object : NsdManager.ServiceInfoCallback {
            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                Timber.w(
                    "Échec d'enregistrement ServiceInfoCallback pour %s : code=%d",
                    serviceName,
                    errorCode
                )
                serviceInfoCallbacks.remove(serviceName, this)
                servicesToResolve.remove(serviceName)

                if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) {
                    retryModernServiceInfoCallback(service)
                }
            }

            override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                serviceInfoRetryCounts.remove(serviceName)
                servicesToResolve.remove(serviceName)
                processResolvedService(serviceInfo)
            }

            override fun onServiceLost() {
                Timber.i("ServiceInfoCallback signale la perte de %s", serviceName)
                emitServiceLost(serviceName)
            }

            override fun onServiceInfoCallbackUnregistered() {
                Timber.d("ServiceInfoCallback désenregistré pour %s", serviceName)
                serviceInfoCallbacks.remove(serviceName, this)
            }
        }

        if (serviceInfoCallbacks.putIfAbsent(serviceName, callback) != null) {
            return
        }

        servicesToResolve.add(serviceName)
        try {
            nsdManager?.registerServiceInfoCallback(service, nsdExecutor, callback)
            Timber.d("ServiceInfoCallback enregistré pour %s", serviceName)
        } catch (e: Exception) {
            serviceInfoCallbacks.remove(serviceName, callback)
            servicesToResolve.remove(serviceName)
            Timber.w(e, "Impossible d'enregistrer ServiceInfoCallback pour %s", serviceName)
        }
    }

    private fun retryModernServiceInfoCallback(service: NsdServiceInfo) {
        val serviceName = service.serviceName ?: return
        val retryCount = serviceInfoRetryCounts.merge(serviceName, 1, Int::plus) ?: 1

        if (retryCount > MAX_RESOLVE_RETRIES) {
            Timber.e("Abandon du suivi NSD de %s après %d tentatives.", serviceName, retryCount - 1)
            serviceInfoRetryCounts.remove(serviceName)
            return
        }

        coroutineScope.launch {
            delay(RESOLVE_RETRY_DELAY_MS * retryCount)
            if (currentDiscoveryListener != null && !resolvedServicesCache.containsKey(serviceName)) {
                Timber.d("Nouvelle tentative ServiceInfoCallback %d/%d pour %s", retryCount, MAX_RESOLVE_RETRIES, serviceName)
                registerModernServiceInfoCallback(service)
            }
        }
    }

    private fun unregisterServiceInfoCallback(serviceName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val callback = serviceInfoCallbacks.remove(serviceName) ?: return
        try {
            nsdManager?.unregisterServiceInfoCallback(callback)
        } catch (e: Exception) {
            Timber.d(e, "Callback ServiceInfo déjà arrêté pour %s", serviceName)
        }
    }

    private fun unregisterAllServiceInfoCallbacks() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            serviceInfoCallbacks.clear()
            return
        }

        serviceInfoCallbacks.entries.toList().forEach { (serviceName, callback) ->
            serviceInfoCallbacks.remove(serviceName, callback)
            try {
                nsdManager?.unregisterServiceInfoCallback(callback)
            } catch (e: Exception) {
                Timber.d(e, "Callback ServiceInfo déjà arrêté pour %s", serviceName)
            }
        }
    }

    /**
     * Android 13 et antérieurs : mise en file stricte des résolutions. Un seul resolveService()
     * est actif à la fois, ce qui évite qu'une seconde TV soit perdue avec FAILURE_ALREADY_ACTIVE.
     */
    private fun enqueueLegacyResolve(service: NsdServiceInfo) {
        val serviceName = service.serviceName ?: return

        synchronized(legacyResolveQueueLock) {
            val alreadyQueued = legacyResolveQueue.any { it.serviceName == serviceName }
            if (
                alreadyQueued ||
                activeLegacyResolveServiceName == serviceName ||
                resolvedServicesCache.containsKey(serviceName)
            ) {
                return
            }

            legacyResolveQueue.addLast(service)
            servicesToResolve.add(serviceName)
        }

        resolveNextLegacyService()
    }

    private fun resolveNextLegacyService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return

        val service: NsdServiceInfo = synchronized(legacyResolveQueueLock) {
            if (activeLegacyResolveServiceName != null) return
            val next = legacyResolveQueue.pollFirst() ?: return
            activeLegacyResolveServiceName = next.serviceName
            next
        }

        val serviceName = service.serviceName ?: run {
            finishLegacyResolve(null)
            return
        }
        val listener = initializeLegacyResolveListener(service)
        activeResolveListeners[serviceName] = listener

        try {
            Timber.d("Résolution NSD séquentielle de %s", serviceName)
            nsdManager?.resolveService(service, listener)
        } catch (e: Exception) {
            Timber.w(e, "Erreur au lancement de la résolution NSD de %s", serviceName)
            servicesToResolve.remove(serviceName)
            activeResolveListeners.remove(serviceName)
            finishLegacyResolve(serviceName)
        }
    }

    private fun initializeLegacyResolveListener(originalService: NsdServiceInfo): NsdManager.ResolveListener {
        val serviceNameKey = originalService.serviceName ?: "Inconnu"

        return object : NsdManager.ResolveListener {
            override fun onResolveFailed(failedServiceInfo: NsdServiceInfo?, errorCode: Int) {
                val name = failedServiceInfo?.serviceName ?: serviceNameKey
                Timber.w("Échec de la résolution NSD pour %s : CodeErreur=%d", name, errorCode)

                servicesToResolve.remove(name)
                activeResolveListeners.remove(name)
                finishLegacyResolve(name)

                if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) {
                    retryLegacyResolve(originalService)
                } else {
                    resolveRetryCounts.remove(name)
                    resolveNextLegacyService()
                }
            }

            override fun onServiceResolved(resolvedServiceInfo: NsdServiceInfo?) {
                servicesToResolve.remove(serviceNameKey)
                activeResolveListeners.remove(serviceNameKey)
                finishLegacyResolve(serviceNameKey)
                resolveRetryCounts.remove(serviceNameKey)

                if (resolvedServiceInfo == null) {
                    Timber.w("Résolution NSD nulle pour %s", serviceNameKey)
                } else {
                    processResolvedService(resolvedServiceInfo)
                }

                resolveNextLegacyService()
            }
        }
    }

    private fun retryLegacyResolve(service: NsdServiceInfo) {
        val serviceName = service.serviceName ?: return
        val retryCount = resolveRetryCounts.merge(serviceName, 1, Int::plus) ?: 1

        if (retryCount > MAX_RESOLVE_RETRIES) {
            Timber.e("Abandon de la résolution de %s après %d tentatives.", serviceName, retryCount - 1)
            resolveRetryCounts.remove(serviceName)
            resolveNextLegacyService()
            return
        }

        coroutineScope.launch {
            delay(RESOLVE_RETRY_DELAY_MS * retryCount)
            if (currentDiscoveryListener != null && !resolvedServicesCache.containsKey(serviceName)) {
                Timber.d("Retry NSD %d/%d pour %s", retryCount, MAX_RESOLVE_RETRIES, serviceName)
                enqueueLegacyResolve(service)
            }
        }
    }

    private fun finishLegacyResolve(serviceName: String?) {
        synchronized(legacyResolveQueueLock) {
            if (serviceName == null || activeLegacyResolveServiceName == serviceName) {
                activeLegacyResolveServiceName = null
            }
        }
    }

    private fun removeFromLegacyResolveQueue(serviceName: String) {
        synchronized(legacyResolveQueueLock) {
            legacyResolveQueue.removeAll { it.serviceName == serviceName }
        }
    }

    private fun processResolvedService(resolvedServiceInfo: NsdServiceInfo) {
        val currentServiceName = resolvedServiceInfo.serviceName ?: run {
            Timber.w("Service résolu avec un nom nul. Ignoré.")
            return
        }

        val port = resolvedServiceInfo.port
        if (port <= 0) {
            Timber.w("Le service résolu %s a un port invalide : %d. Ignoré.", currentServiceName, port)
            return
        }

        val hostAddresses: List<InetAddress> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            resolvedServiceInfo.hostAddresses?.takeIf { it.isNotEmpty() }
                ?: resolvedServiceInfo.host?.let { listOf(it) }
                ?: emptyList()
        } else {
            resolvedServiceInfo.host?.let { listOf(it) } ?: emptyList()
        }

        if (hostAddresses.isEmpty()) {
            Timber.w("Le service résolu %s n'a aucune adresse d'hôte. Ignoré.", currentServiceName)
            return
        }

        // Les sockets Remote v2 fonctionnent parfaitement en IPv4 et cela évite de choisir
        // accidentellement une IPv6 link-local quand Android renvoie plusieurs adresses.
        val hostAddress = hostAddresses.firstOrNull { it is Inet4Address } ?: hostAddresses.first()

        val attributes = decodeAttributes(resolvedServiceInfo)
        val friendlyName = attributes["fn"]?.takeIf { it.isNotBlank() } ?: currentServiceName

        val tv = DiscoveredTv(
            serviceName = currentServiceName,
            friendlyName = friendlyName,
            hostAddress = hostAddress,
            port = port,
            attributes = attributes
        )

        Timber.i(
            "TV NSD résolue : nom=%s, ip=%s, port=%d, txt=%s",
            tv.friendlyName,
            tv.ipAddress,
            tv.port,
            tv.attributes
        )

        val previous = resolvedServicesCache.put(currentServiceName, tv)
        when {
            previous == null -> {
                _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv))
                Timber.i("TV trouvée et émise : %s", tv.friendlyName)
            }

            previous != tv -> {
                _eventFlow.tryEmit(DiscoveryEvent.TvLost(previous))
                _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv))
                Timber.i("TV mise à jour et réémise : %s", tv.friendlyName)
            }

            else -> Timber.d("Service %s inchangé, notification dupliquée ignorée.", currentServiceName)
        }
    }

    private fun decodeAttributes(serviceInfo: NsdServiceInfo): Map<String, String> {
        return try {
            serviceInfo.attributes.orEmpty().mapValues { (_, value) ->
                try {
                    String(value, Charsets.UTF_8)
                } catch (_: Exception) {
                    value.joinToString(separator = "") { byte -> "%02X".format(byte) }
                }
            }
        } catch (e: Exception) {
            Timber.d(e, "Impossible de lire les attributs TXT de %s", serviceInfo.serviceName)
            emptyMap()
        }
    }

    private fun emitServiceLost(serviceName: String) {
        val lostTv = resolvedServicesCache.remove(serviceName)
        if (lostTv != null) {
            _eventFlow.tryEmit(DiscoveryEvent.TvLost(lostTv))
        }
    }

    private fun acquireMulticastLock() {
        if (nsdManager == null) return

        if (multicastLock == null) {
            val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifi != null) {
                multicastLock = wifi.createMulticastLock("${this.javaClass.simpleName}.MulticastLock").apply {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        setReferenceCounted(true)
                    }
                }
            } else {
                Timber.e("WifiManager non disponible, impossible de créer MulticastLock.")
                return
            }
        }

        try {
            multicastLock?.let {
                if (!it.isHeld) {
                    it.acquire()
                    Timber.i("MulticastLock acquis.")
                } else {
                    Timber.d("MulticastLock déjà détenu.")
                }
            }
        } catch (se: SecurityException) {
            Timber.e(
                se,
                "SecurityException lors de l'acquisition de MulticastLock. Vérifiez CHANGE_WIFI_MULTICAST_STATE."
            )
            _eventFlow.tryEmit(
                DiscoveryEvent.Error(
                    "Permission manquante pour MulticastLock (CHANGE_WIFI_MULTICAST_STATE)"
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'acquisition de MulticastLock.")
        }
    }

    private fun releaseMulticastLock() {
        multicastLock?.let {
            if (it.isHeld) {
                try {
                    it.release()
                    Timber.i("MulticastLock libéré.")
                } catch (e: Exception) {
                    Timber.w(e, "Erreur lors de la libération de MulticastLock.")
                }
            } else {
                Timber.d("MulticastLock non détenu, pas besoin de le libérer.")
            }
        }
    }

    fun cleanup() {
        Timber.i("Nettoyage de TvDiscoveryManager.")
        coroutineScope.launch {
            discoveryLock.withLock {
                stopDiscoveryInternal(notifyListener = false, releaseLock = true)
            }
        }.invokeOnCompletion {
            coroutineScope.cancel()
            Timber.i("CoroutineScope de TvDiscoveryManager annulé.")
        }
    }
}
