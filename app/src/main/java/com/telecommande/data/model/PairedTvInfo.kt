package com.telecommande.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.telecommande.core.protocol.TvProtocolType
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "paired_tvs")
data class PairedTvInfo(
    @PrimaryKey
    val keystoreAlias: String,
    val ipAddress: String,
    val name: String?,
    val macAddress: String?,
    val protocolType: String = TvProtocolType.ANDROID_TV_REMOTE_V2.name
)
