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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.net.InetAddress
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class TvDiscoveryManager(context: Context) {
    private val appContext: Context = context.applicationContext
    private val nsdManager: NsdManager? = appContext.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Erreur non interceptée dans TvDiscoveryManager")
    })

    private var currentDiscoveryListener: NsdManager.DiscoveryListener? = null
    private val activeResolveListeners = ConcurrentHashMap<String, NsdManager.ResolveListener>()

    private var multicastLock: WifiManager.MulticastLock? = null
    private val nsdExecutor: Executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "NsdResolveThread").apply { isDaemon = true }
    }

    private val servicesToResolve = Collections.synchronizedSet(HashSet<String>())
    private val resolvedServicesCache = ConcurrentHashMap<String, DiscoveredTv>()
    private val discoveryLock = Mutex()

    private val _eventFlow = MutableSharedFlow<DiscoveryEvent>(replay = 0, extraBufferCapacity = 10)
    val eventFlow: Flow<DiscoveryEvent> = _eventFlow.asSharedFlow()

    companion object {
        const val SERVICE_TYPE_ANDROID_TV_REMOTE = "_androidtvremote2._tcp."
    }

    init {
        if (nsdManager == null) {
            Timber.e("NsdManager n'est pas disponible. La découverte de services ne fonctionnera pas.")
            coroutineScope.launch { _eventFlow.tryEmit(DiscoveryEvent.Error("NsdManager non disponible")) }
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
                    Timber.w("La découverte est déjà active. Tentative d'arrêt de la précédente avant de redémarrer.")
                    stopDiscoveryInternal(notifyListener = false, releaseLock = false)
                }

                Timber.i("Démarrage de la découverte de TV pour le type de service : %s", SERVICE_TYPE_ANDROID_TV_REMOTE)
                acquireMulticastLock()
                servicesToResolve.clear()
                resolvedServicesCache.forEach { (_, tv) ->
                    _eventFlow.tryEmit(DiscoveryEvent.TvLost(tv))
                }
                resolvedServicesCache.clear()

                val listener = initializeNsdDiscoveryListener()
                currentDiscoveryListener = listener
                try {
                    nsdManager.discoverServices(
                        SERVICE_TYPE_ANDROID_TV_REMOTE,
                        NsdManager.PROTOCOL_DNS_SD,
                        listener
                    )
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "Erreur au démarrage de la découverte (écouteur déjà enregistré ou autre problème)")
                    _eventFlow.tryEmit(DiscoveryEvent.Error("Échec du démarrage de la découverte: ${e.message}", 0))
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
                Timber.w(e, "Erreur lors de l'arrêt de la découverte (l'écouteur pourrait ne pas être enregistré ou autre).")
            } finally {
                currentDiscoveryListener = null
            }
        }

        servicesToResolve.clear()
        activeResolveListeners.clear()

        if (releaseLock) {
            releaseMulticastLock()
        }

        if (notifyListener) {
            _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStopped)
        }
    }

    private fun initializeNsdDiscoveryListener(): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Timber.d("Découverte NSD démarrée : %s", regType)
                _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStarted)
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Timber.i("Service NSD trouvé : Nom=%s, Type=%s", service.serviceName, service.serviceType)

                var currentServiceType = service.serviceType
                if (currentServiceType == null) {
                    Timber.w("Service trouvé avec un type nul. Ignoré.")
                    return
                }
                if (!currentServiceType.endsWith(".")) {
                    currentServiceType += "."
                }

                if (!SERVICE_TYPE_ANDROID_TV_REMOTE.equals(currentServiceType, ignoreCase = true)) {
                    Timber.d("Ignorance du service avec le type : %s (Attendu : %s)", service.serviceType, SERVICE_TYPE_ANDROID_TV_REMOTE)
                    return
                }

                val serviceName = service.serviceName ?: run {
                    Timber.w("Service trouvé avec un nom de service nul. Ignoré.")
                    return
                }

                if (servicesToResolve.contains(serviceName) || resolvedServicesCache.containsKey(serviceName)) {
                    Timber.d("Le service %s est déjà en cours de résolution ou a été résolu. Ignoré.", serviceName)
                    return
                }
                servicesToResolve.add(serviceName)

                Timber.d("Planification de la résolution du service : %s", serviceName)
                val resolveListener = initializeResolveListener(serviceName)
                activeResolveListeners[serviceName] = resolveListener

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        Timber.d("Utilisation de resolveService avec Executor pour %s", serviceName)
                        nsdManager?.resolveService(service, nsdExecutor, resolveListener)
                    } else {
                        Timber.d("Utilisation de l'ancienne API resolveService pour %s", serviceName)
                        nsdManager?.resolveService(service, resolveListener)
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "Erreur lors de la tentative de résolution du service %s", serviceName)
                    servicesToResolve.remove(serviceName)
                    activeResolveListeners.remove(serviceName)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                val serviceName = service.serviceName ?: "Inconnu"
                Timber.i("Service NSD perdu : %s", serviceName)
                servicesToResolve.remove(serviceName)
                activeResolveListeners.remove(serviceName)
                val lostTv = resolvedServicesCache.remove(serviceName)

                if (lostTv != null) {
                    _eventFlow.tryEmit(DiscoveryEvent.TvLost(lostTv))
                } else if (service.serviceName != null) {
                    Timber.w("Service perdu (%s) mais non trouvé dans la liste des services résolus ou détails incomplets.", serviceName)
                    _eventFlow.tryEmit(DiscoveryEvent.TvLost(DiscoveredTv(serviceName, serviceName, null, 0)))
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Timber.d("Découverte NSD explicitement arrêtée pour : %s", serviceType)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Timber.e("Échec du démarrage de la découverte NSD : Type=%s, CodeErreur=%d", serviceType, errorCode)
                _eventFlow.tryEmit(DiscoveryEvent.Error("Échec du démarrage de la découverte.", errorCode))
                coroutineScope.launch {
                    discoveryLock.withLock {
                        stopDiscoveryInternal(notifyListener = false, releaseLock = true)
                    }
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Timber.e("Échec de l'arrêt de la découverte NSD : Type=%s, CodeErreur=%d", serviceType, errorCode)
                _eventFlow.tryEmit(DiscoveryEvent.Error("Échec de l'arrêt de la découverte.", errorCode))
                releaseMulticastLock()
            }
        }
    }

    private fun initializeResolveListener(serviceNameKey: String): NsdManager.ResolveListener {
        return object : NsdManager.ResolveListener {
            override fun onResolveFailed(failedServiceInfo: NsdServiceInfo?, errorCode: Int) {
                val name = failedServiceInfo?.serviceName ?: serviceNameKey
                Timber.e("Échec de la résolution NSD pour %s : CodeErreur=%d", name, errorCode)
                servicesToResolve.remove(name)
                activeResolveListeners.remove(name)
            }

            override fun onServiceResolved(resolvedServiceInfo: NsdServiceInfo?) {
                if (resolvedServiceInfo == null) {
                    Timber.w("onServiceResolved appelé avec serviceInfo nul pour la clé %s. Ignoré.", serviceNameKey)
                    servicesToResolve.remove(serviceNameKey)
                    activeResolveListeners.remove(serviceNameKey)
                    return
                }

                val currentServiceName = resolvedServiceInfo.serviceName ?: run {
                    Timber.w("Service résolu avec un nom de service nul (clé originale: %s). Ignoré.", serviceNameKey)
                    servicesToResolve.remove(serviceNameKey)
                    activeResolveListeners.remove(serviceNameKey)
                    return
                }

                servicesToResolve.remove(serviceNameKey)
                activeResolveListeners.remove(serviceNameKey)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Timber.i("Service NSD résolu : Nom=%s, Hôtes=%s, Port=%d", resolvedServiceInfo.serviceName, resolvedServiceInfo.hostAddresses?.joinToString(), resolvedServiceInfo.port)
                } else {
                    Timber.i("Service NSD résolu : Nom=%s, Hôte=%s, Port=%d", resolvedServiceInfo.serviceName, resolvedServiceInfo.host, resolvedServiceInfo.port)
                }

                if (resolvedServicesCache.containsKey(currentServiceName)) {
                    Timber.d("Le service %s a déjà été résolu et notifié. Ignorance de la notification en double.", currentServiceName)
                    return
                }

                var friendlyName = currentServiceName
                val port = resolvedServiceInfo.port
                val hostAddresses: List<InetAddress>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    resolvedServiceInfo.hostAddresses?.takeIf { it.isNotEmpty() }
                } else {
                    resolvedServiceInfo.host?.let { listOf(it) }
                }

                if (hostAddresses.isNullOrEmpty()) {
                    Timber.w("Le service résolu %s a des adresses d'hôte nulles ou vides. Ignoré.", currentServiceName)
                    return
                }
                val hostAddress = hostAddresses[0]
                Timber.d("Utilisation de l'adresse IP: %s pour %s", hostAddress, currentServiceName)

                resolvedServiceInfo.attributes?.get("fn")?.let { fnBytes ->
                    try {
                        friendlyName = String(fnBytes, Charsets.UTF_8)
                        Timber.d("Nom convivial à partir de l'enregistrement TXT : %s", friendlyName)
                    } catch (e: Exception) {
                        Timber.w(e, "Échec du décodage du nom convivial à partir de l'enregistrement TXT")
                    }
                }

                if (port <= 0) {
                    Timber.w("Le service résolu %s a un port invalide : %d. Ignoré.", currentServiceName, port)
                    return
                }

                val tv = DiscoveredTv(currentServiceName, friendlyName, hostAddress, port)
                Timber.d("Service résolu et traité avec succès : %s", tv)

                if (resolvedServicesCache.putIfAbsent(currentServiceName, tv) == null) {
                    _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv))
                    Timber.i("TV trouvée et émise : %s", tv.friendlyName)
                } else {
                    Timber.d("Service %s déjà dans le cache, notification TvFound ignorée.", currentServiceName)
                }
            }
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
            Timber.e(se, "SecurityException lors de l'acquisition de MulticastLock. Vérifiez la permission CHANGE_WIFI_MULTICAST_STATE.")
            _eventFlow.tryEmit(DiscoveryEvent.Error("Permission manquante pour MulticastLock (CHANGE_WIFI_MULTICAST_STATE)"))
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
