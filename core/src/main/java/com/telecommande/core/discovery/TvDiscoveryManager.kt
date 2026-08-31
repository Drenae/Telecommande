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

/** Orchestrateur de découverte TV par mDNS direct. */
class TvDiscoveryManager(context: Context) {
    private val appContext = context.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { _, t -> Timber.e(t, "Erreur non interceptée dans TvDiscoveryManager") })
    private val discoveryLock = Mutex()
    private val resolvedServicesCache = ConcurrentHashMap<String, DiscoveredTv>()
    private var multicastLock: WifiManager.MulticastLock? = null
    private var isDiscovering = false
    private val _eventFlow = MutableSharedFlow<DiscoveryEvent>(replay = 0, extraBufferCapacity = 32)
    val eventFlow: Flow<DiscoveryEvent> = _eventFlow.asSharedFlow()

    private val mdnsEngine = MdnsDiscoveryEngine(
        context = appContext,
        scope = coroutineScope,
        onFound = ::handleMdnsFound,
        onLost = ::handleMdnsLost
    )

    fun startDiscovery() { coroutineScope.launch { discoveryLock.withLock {
        if (isDiscovering) return@withLock
        Timber.i("Démarrage de la découverte mDNS directe : %s", SERVICE_TYPE_ANDROID_TV_REMOTE)
        clearResolvedServices(true); acquireMulticastLock(); isDiscovering = true
        _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStarted); mdnsEngine.start()
    } } }

    fun stopDiscovery() { coroutineScope.launch { discoveryLock.withLock { stopDiscoveryInternal(true) } } }

    private fun stopDiscoveryInternal(notifyListener: Boolean) {
        if (!isDiscovering && !notifyListener) return
        Timber.i("Arrêt de la découverte mDNS directe."); isDiscovering = false
        mdnsEngine.stop(false); clearResolvedServices(true); releaseMulticastLock()
        if (notifyListener) _eventFlow.tryEmit(DiscoveryEvent.DiscoveryStopped)
    }

    private fun handleMdnsFound(tv: DiscoveredTv) {
        if (!isDiscovering) return
        val key = serviceKey(tv); val previous = resolvedServicesCache.put(key, tv)
        when {
            previous == null -> { Timber.i("TV mDNS émise : nom=%s, ip=%s, port=%d, txt=%s", tv.friendlyName, tv.ipAddress, tv.port, tv.attributes); _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv)) }
            previous != tv -> { _eventFlow.tryEmit(DiscoveryEvent.TvLost(previous)); _eventFlow.tryEmit(DiscoveryEvent.TvFound(tv)) }
        }
    }

    private fun handleMdnsLost(tv: DiscoveredTv) {
        val removed = resolvedServicesCache.remove(serviceKey(tv)) ?: return
        Timber.i("TV mDNS perdue : %s", removed.friendlyName); _eventFlow.tryEmit(DiscoveryEvent.TvLost(removed))
    }

    private fun serviceKey(tv: DiscoveredTv): String = tv.attributes["bt"]?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: tv.serviceName.lowercase()
    private fun clearResolvedServices(emitLost: Boolean) { if (emitLost) resolvedServicesCache.values.toList().forEach { _eventFlow.tryEmit(DiscoveryEvent.TvLost(it)) }; resolvedServicesCache.clear() }

    private fun acquireMulticastLock() {
        if (multicastLock == null) {
            val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifi == null) { _eventFlow.tryEmit(DiscoveryEvent.Error("WifiManager non disponible")); return }
            multicastLock = wifi.createMulticastLock("${this.javaClass.simpleName}.MulticastLock").apply { if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) setReferenceCounted(true) }
        }
        try { multicastLock?.let { if (!it.isHeld) { it.acquire(); Timber.i("MulticastLock acquis pour mDNS direct.") } } }
        catch (e: Exception) { Timber.e(e, "Impossible d'activer le multicast"); _eventFlow.tryEmit(DiscoveryEvent.Error("Impossible d'activer le multicast: ${e.message}")) }
    }

    private fun releaseMulticastLock() { multicastLock?.let { if (it.isHeld) try { it.release() } catch (_: Exception) {} } }
    fun cleanup() { coroutineScope.launch { discoveryLock.withLock { stopDiscoveryInternal(false) } }.invokeOnCompletion { coroutineScope.cancel() } }

    companion object { const val SERVICE_TYPE_ANDROID_TV_REMOTE = "_androidtvremote2._tcp." }
}
