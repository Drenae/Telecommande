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
import kotlinx.serialization.decodeFromString
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
        private val TV_DISPLAY_NAMES_KEY = stringPreferencesKey("tv_display_names_v1")
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
            jsonString?.let {
                try {
                    Json.decodeFromString<PairedTvInfo>(it)
                } catch (e: Exception) {
                    Timber.tag("SettingsRepo").e(e, "Échec du décodage des informations de la TV active")
                    null
                }
            }
        }

    override val pairedTvsFlow: Flow<List<PairedTvInfo>> = pairedTvDao.getAllFlow()
        .catch { e ->
            Timber.tag("SettingsRepo").e(e, "Erreur de lecture des TVs appairées depuis Room")
            emit(emptyList())
        }

    override val tvDisplayNamesFlow: Flow<Map<String, String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.tag("SettingsRepo").w(exception, "Erreur de lecture des noms personnalisés des TV")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[TV_DISPLAY_NAMES_KEY]?.let { jsonString ->
                try {
                    Json.decodeFromString<Map<String, String>>(jsonString)
                } catch (e: Exception) {
                    Timber.tag("SettingsRepo").e(e, "Échec du décodage des noms personnalisés des TV")
                    emptyMap()
                }
            } ?: emptyMap()
        }

    override suspend fun saveActiveTvInfo(tvInfo: PairedTvInfo?) {
        dataStore.edit { settings ->
            if (tvInfo == null) {
                settings.remove(ACTIVE_TV_INFO_KEY)
                return@edit
            }

            try {
                settings[ACTIVE_TV_INFO_KEY] = Json.encodeToString(tvInfo)
            } catch (e: Exception) {
                Timber.tag("SettingsRepo").e(e, "Échec de l'encodage des informations de la TV active")
                settings.remove(ACTIVE_TV_INFO_KEY)
            }
        }
    }

    override suspend fun getActiveTvInfo(): PairedTvInfo? = activeTvInfoFlow.firstOrNull()

    override suspend fun clearActiveTvInfo() {
        dataStore.edit { settings ->
            settings.remove(ACTIVE_TV_INFO_KEY)
        }
    }

    override suspend fun addPairedTv(tvInfo: PairedTvInfo) {
        pairedTvDao.insertOrUpdate(tvInfo)
    }

    override suspend fun removePairedTvByKeystoreAlias(keystoreAlias: String) {
        pairedTvDao.deleteByKeystoreAlias(keystoreAlias)
        setTvDisplayName(keystoreAlias, null)

        val activeTv = getActiveTvInfo()
        if (activeTv?.keystoreAlias == keystoreAlias) {
            clearActiveTvInfo()
        }
    }

    override suspend fun getPairedTvByKeystoreAlias(keystoreAlias: String): PairedTvInfo? {
        return pairedTvDao.getByKeystoreAlias(keystoreAlias)
    }

    override suspend fun setTvDisplayName(keystoreAlias: String, displayName: String?) {
        dataStore.edit { settings ->
            val currentNames = settings[TV_DISPLAY_NAMES_KEY]?.let { jsonString ->
                try {
                    Json.decodeFromString<Map<String, String>>(jsonString)
                } catch (_: Exception) {
                    emptyMap()
                }
            } ?: emptyMap()

            val updatedNames = currentNames.toMutableMap()
            val normalizedName = displayName?.trim()?.takeIf { it.isNotBlank() }
            if (normalizedName == null) {
                updatedNames.remove(keystoreAlias)
            } else {
                updatedNames[keystoreAlias] = normalizedName
            }

            settings[TV_DISPLAY_NAMES_KEY] = Json.encodeToString(updatedNames)
        }
    }
}
