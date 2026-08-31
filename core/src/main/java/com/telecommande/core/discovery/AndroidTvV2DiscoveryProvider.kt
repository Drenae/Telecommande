package com.telecommande.core.discovery

import android.content.Context
import com.telecommande.core.protocol.TvProtocolType
import kotlinx.coroutines.CoroutineScope

class AndroidTvV2DiscoveryProvider(
    context: Context,
    scope: CoroutineScope,
    onFound: (DiscoveredTv) -> Unit,
    onLost: (DiscoveredTv) -> Unit
) : TvDiscoveryProvider {

    override val protocolType: TvProtocolType = TvProtocolType.ANDROID_TV_REMOTE_V2
    override val name: String = "Android TV Remote v2 (mDNS)"

    private val engine = MdnsDiscoveryEngine(
        context = context.applicationContext,
        scope = scope,
        onFound = onFound,
        onLost = onLost
    )

    override fun start() = engine.start()

    override fun stop() = engine.stop(false)
}
