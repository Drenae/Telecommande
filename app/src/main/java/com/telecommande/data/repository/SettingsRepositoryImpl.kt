package com.telecommande.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.telecommande.data.dao.PairedTvDao
import com.telecommande.data.model.PairedTvInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings_v3")

class SettingsRepositoryImpl @Inject constructor(
    private val pairedTvDao: PairedTvDao,
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        private val ACTIVE_TV_INFO_KEY = stringPreferencesKey("active_tv_info_serialized_v2")
    }

    override val activeTvInfoFlow: Flow<PairedTvInfo?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.tag("SettingsRepo").w(exception, "Erreur de lecture du DataStore pour la TV active")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[ACTIVE_TV_INFO_KEY]
            Timber.tag("SettingsRepo").d("Lecture de active_tv_info_serialized_v2 depuis DataStore. JSON: $jsonString")
            jsonString?.let {
                try {
                    val decodedTv = Json.decodeFromString<PairedTvInfo>(it)
                    Timber.tag("SettingsRepo").d("TV active décodée depuis DataStore : ${decodedTv.name}")
                    decodedTv
                } catch (e: Exception) {
                    Timber.tag("SettingsRepo").e(e, "Échec du décodage des informations de la TV active depuis le DataStore. JSON était: $it")
                    null
                }
            }
        }

    override suspend fun saveActiveTvInfo(tvInfo: PairedTvInfo?) {
        dataStore.edit { settings ->
            if (tvInfo != null) {
                try {
                    val jsonString = Json.encodeToString(tvInfo)
                    settings[ACTIVE_TV_INFO_KEY] = jsonString
                    Timber.tag("SettingsRepo").i("TV active sauvegardée dans DataStore : ${tvInfo.name} - JSON: $jsonString")
                } catch (e: Exception) {
                    Timber.tag("SettingsRepo").e(e, "Échec de l'encodage des informations de la TV active vers le DataStore")
                    settings.remove(ACTIVE_TV_INFO_KEY)
                }
            } else {
                settings.remove(ACTIVE_TV_INFO_KEY)
                Timber.tag("SettingsRepo").i("TV active effacée du DataStore (tvInfo était null).")
            }
        }
    }

    override suspend fun getActiveTvInfo(): PairedTvInfo? {
        return activeTvInfoFlow.firstOrNull()
    }

    override suspend fun clearActiveTvInfo() {
        dataStore.edit { settings ->
            settings.remove(ACTIVE_TV_INFO_KEY)
        }
    }

    override val pairedTvsFlow: Flow<List<PairedTvInfo>> = pairedTvDao.getAllFlow()
        .catch { e ->
            Timber.tag("SettingsRepo").e(e, "Erreur de lecture des TVs appairées depuis Room")
            emit(emptyList())
        }

    override suspend fun addPairedTv(tvInfo: PairedTvInfo) {
        try {
            pairedTvDao.insertOrUpdate(tvInfo)
            Timber.tag("SettingsRepo").d("TV appairée ajoutée/mise à jour : ${tvInfo.keystoreAlias}")
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de l'ajout de la TV appairée à Room : ${tvInfo.keystoreAlias}")
        }
    }

    override suspend fun removePairedTvByKeystoreAlias(keystoreAlias: String) {
        try {
            pairedTvDao.deleteByKeystoreAlias(keystoreAlias)
            Timber.tag("SettingsRepo").d("TV appairée supprimée par keystoreAlias : $keystoreAlias")

            val activeTv = getActiveTvInfo()
            if (activeTv?.keystoreAlias == keystoreAlias) {
                clearActiveTvInfo()
                Timber.tag("SettingsRepo").d("TV active effacée car elle a été supprimée : $keystoreAlias")
            }
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de la suppression de la TV appairée par keystoreAlias de Room : $keystoreAlias")
        }
    }

    override suspend fun removePairedTvByIpAddress(tvIpAddress: String) {
        try {
            val tvToRemove = pairedTvDao.getByIpAddress(tvIpAddress)
            if (tvToRemove != null) {
                pairedTvDao.delete(tvToRemove)
                Timber.tag("SettingsRepo").d("TV appairée supprimée par IP : $tvIpAddress, (Alias: ${tvToRemove.keystoreAlias})")

                val activeTv = getActiveTvInfo()
                if (activeTv?.ipAddress == tvIpAddress) {
                    clearActiveTvInfo()
                    Timber.tag("SettingsRepo").d("TV active effacée car elle a été supprimée (par IP) : $tvIpAddress")
                }
            } else {
                Timber.tag("SettingsRepo").w("Tentative de suppression de TV par IP $tvIpAddress, mais non trouvée dans Room.")
            }
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de la suppression de la TV appairée par IP de Room : $tvIpAddress")
        }
    }

    override suspend fun getPairedTvByKeystoreAlias(keystoreAlias: String): PairedTvInfo? {
        return try {
            pairedTvDao.getByKeystoreAlias(keystoreAlias)
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de la récupération de la TV appairée par keystoreAlias de Room : $keystoreAlias")
            null
        }
    }

    override suspend fun getPairedTvByIpAddress(tvIpAddress: String): PairedTvInfo? {
        return try {
            pairedTvDao.getByIpAddress(tvIpAddress)
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de la récupération de la TV appairée par IP de Room : $tvIpAddress")
            null
        }
    }

    override suspend fun getAllPairedTvs(): List<PairedTvInfo> {
        return try {
            pairedTvDao.getAll()
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de la récupération de toutes les TVs appairées de Room")
            emptyList()
        }
    }

    override suspend fun clearAllPairedTvs() {
        try {
            pairedTvDao.clearAll()
            clearActiveTvInfo()
            Timber.tag("SettingsRepo").d("Toutes les TVs appairées ont été effacées de Room ainsi que la TV active.")
        } catch (e: Exception) {
            Timber.tag("SettingsRepo").e(e, "Erreur lors de l'effacement de toutes les TVs appairées de Room")
        }
    }
}