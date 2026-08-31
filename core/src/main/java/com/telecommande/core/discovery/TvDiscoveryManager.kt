package com.telecommande.core.discovery

import android.content.Context
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
import java.util.concurrent.ConcurrentHashMap

/**
 * Orchestrateur de découverte TV.
 *
 * Depuis la V2, la découverte Android TV Remote v2 n'utilise plus NsdManager. Le moteur mDNS
 * interroge directement le multicast DNS afin de ne pas dépendre des callbacks NSD incomplets
 * observés avec plusieurs TV sur le même réseau.
 */
class TvDiscoveryManager(context: Context) {
    private val appContext = context.applicationContext

    private val coroutineScope = CoroutineScope(
        Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Erreur non interceptée dans TvDiscoveryManager")
        }
    )

    private val discoveryLock = Mutex()
    private val resolvedServicesCache = ConcurrentHashMap<String, DiscoveredTv>()
    private var multicastLock: WifiManager.MulticastLock? = null
    private var isDiscovering = false

    private val _eventFlow = MutableSharedFlow<DiscoveryEvent>(
        replay = 0,
        extraBufferCapacity = 32
    )
    val eventFlow: Flow<DiscoveryEvent> = _eventFlow.asSharedFlow()

    private val mdnsEngine = MdnsDiscoveryEngine(
        scope = coroutineScope,
        onFound = ::handleMdnsFound,
        onLost = ::handleMdnsLost
    )

    fun startDiscovery() {
        coroutineScope.launch {
            discoveryLock.withLock {
                if (isDiscovering) {
                    Timber.d("Découverte mDNS déjà active.")
                    return@withLock
                }

                Timber.i(
                    "Démarrage de la découverte mDNS directe : %s",
                    SERVICE_TYPE_ANDROID_TV_REMOTE
                )

                clearResolvedServices(emitLost = true)
                acquireMulticastLock()
                isDiscovering = true
                _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStarted)
                mdnsEngine.start()
            }
        }
    }

    fun stopDiscovery() {
        coroutineScope.launch {
            discoveryLock.withLock {
                stopDiscoveryInternal(notifyListener = true)
            }
        }
    }

    private fun stopDiscoveryInternal(notifyListener: Boolean) {
        if (!isDiscovering && !notifyListener) return

        Timber.i("Arrêt de la découverte mDNS directe.")
        isDiscovering = false
        mdnsEngine.stop(emitLost = false)
        clearResolvedServices(emitLost = true)
        releaseMulticastLock()

        if (notifyListener) {
            _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStopped)
        }
    }

    private fun handleMdnsFound(tv: DiscoveredTv) {
        if (!isDiscovering) return

        val key = serviceKey(tv)
        val previous = resolvedServicesCache.put(key, tv)

        when {
            previous == null -> {
                Timber.i(
                    "TV mDNS émise : nom=%s, ip=%s, port=%d, txt=%s",
                    tv.friendlyName,
                    tv.ipAddress,
                    tv.port,
                    tv.attributes
                )
                _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv))
            }

            previous != tv -> {
                Timber.i(
                    "TV mDNS mise à jour : nom=%s, ip=%s, port=%d",
                    tv.friendlyName,
                    tv.ipAddress,
                    tv.port
                )
                _eventFlow.tryEmit(DiscoveryEvent.TvLost(previous))
                _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv))
            }

            else -> Timber.v("Annonce mDNS dupliquée ignorée pour %s", tv.serviceName)
        }
    }

    private fun handleMdnsLost(tv: DiscoveredTv) {
        val key = serviceKey(tv)
        val removed = resolvedServicesCache.remove(key) ?: return
        Timber.i("TV mDNS perdue : %s", removed.friendlyName)
        _eventFlow.tryEmit(DiscoveryEvent.TvLost(removed))
    }

    /**
     * Le TXT `bt` est l'identifiant le plus stable actuellement exposé par Android TV Remote v2.
     * Il sert uniquement à dédupliquer deux annonces du même appareil, jamais à filtrer une TV.
     */
    private fun serviceKey(tv: DiscoveredTv): String {
        val stableId = tv.attributes["bt"]?.trim()?.lowercase()
        return stableId?.takeIf { it.isNotBlank() }
            ?: tv.serviceName.lowercase()
    }

    private fun clearResolvedServices(emitLost: Boolean) {
        if (emitLost) {
            resolvedServicesCache.values.toList().forEach { tv ->
                _eventFlow.tryEmit(DiscoveryEvent.TvLost(tv))
            }
        }
        resolvedServicesCache.clear()
    }

    private fun acquireMulticastLock() {
        if (multicastLock == null) {
            val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifi == null) {
                Timber.e("WifiManager non disponible, impossible de créer MulticastLock.")
                _eventFlow.tryEmit(DiscoveryEvent.Error("WifiManager non disponible"))
                return
            }

            multicastLock = wifi.createMulticastLock("${this.javaClass.simpleName}.MulticastLock").apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    setReferenceCounted(true)
                }
            }
        }

        try {
            multicastLock?.let { lock ->
                if (!lock.isHeld) {
                    lock.acquire()
                    Timber.i("MulticastLock acquis pour mDNS direct.")
                }
            }
        } catch (se: SecurityException) {
            Timber.e(se, "Permission CHANGE_WIFI_MULTICAST_STATE manquante.")
            _eventFlow.tryEmit(
                DiscoveryEvent.Error(
                    "Permission manquante pour le multicast (CHANGE_WIFI_MULTICAST_STATE)"
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'acquisition du MulticastLock.")
            _eventFlow.tryEmit(DiscoveryEvent.Error("Impossible d'activer le multicast: ${e.message}"))
        }
    }

    private fun releaseMulticastLock() {
        multicastLock?.let { lock ->
            if (lock.isHeld) {
                try {
                    lock.release()
                    Timber.i("MulticastLock mDNS libéré.")
                } catch (e: Exception) {
                    Timber.w(e, "Erreur lors de la libération du MulticastLock.")
                }
            }
        }
    }

    fun cleanup() {
        Timber.i("Nettoyage de TvDiscoveryManager.")
        coroutineScope.launch {
            discoveryLock.withLock {
                stopDiscoveryInternal(notifyListener = false)
            }
        }.invokeOnCompletion {
            coroutineScope.cancel()
        }
    }

    companion object {
        const val SERVICE_TYPE_ANDROID_TV_REMOTE = "_androidtvremote2._tcp."
    }
}
