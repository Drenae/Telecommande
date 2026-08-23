package com.telecommande.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.telecommande.data.model.PairedTvInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedTvDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tvInfo: PairedTvInfo)

    @Delete
    suspend fun delete(tvInfo: PairedTvInfo)

    @Query("DELETE FROM paired_tvs WHERE keystoreAlias = :keystoreAlias")
    suspend fun deleteByKeystoreAlias(keystoreAlias: String)

    @Query("DELETE FROM paired_tvs WHERE ipAddress = :ipAddress")
    suspend fun deleteByIpAddress(ipAddress: String)


    @Query("SELECT * FROM paired_tvs WHERE keystoreAlias = :keystoreAlias")
    suspend fun getByKeystoreAlias(keystoreAlias: String): PairedTvInfo?

    @Query("SELECT * FROM paired_tvs WHERE ipAddress = :ipAddress")
    suspend fun getByIpAddress(ipAddress: String): PairedTvInfo?

    @Query("SELECT * FROM paired_tvs")
    fun getAllFlow(): Flow<List<PairedTvInfo>>

    @Query("SELECT * FROM paired_tvs")
    suspend fun getAll(): List<PairedTvInfo>

    @Query("DELETE FROM paired_tvs")
    suspend fun clearAll()
}