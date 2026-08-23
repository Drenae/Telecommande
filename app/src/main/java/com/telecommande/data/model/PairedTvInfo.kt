package com.telecommande.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "paired_tvs")
data class PairedTvInfo(
    @PrimaryKey
    val keystoreAlias: String,
    val ipAddress: String,
    val name: String?,
    val macAddress: String?
)