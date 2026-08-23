package com.telecommande.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.telecommande.core.AndroidRemoteTv
import com.telecommande.core.discovery.TvDiscoveryManager
import com.telecommande.data.dao.PairedTvDao
import com.telecommande.data.repository.SettingsRepository
import com.telecommande.data.repository.SettingsRepositoryImpl
import com.telecommande.data.repository.discovery.DiscoveryRepository
import com.telecommande.data.repository.discovery.DiscoveryRepositoryImpl
import com.telecommande.data.repository.pairing.PairingRepository
import com.telecommande.data.repository.pairing.PairingRepositoryImpl
import com.telecommande.data.repository.remote.RemoteRepository
import com.telecommande.data.repository.remote.RemoteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        pairedTvDao: PairedTvDao,
        dataStore: DataStore<Preferences>
    ): SettingsRepository {
        return SettingsRepositoryImpl(pairedTvDao, dataStore)
    }

    @Provides
    @Singleton
    fun provideDiscoveryRepository(
        tvDiscoveryManager: TvDiscoveryManager
    ): DiscoveryRepository {
        return DiscoveryRepositoryImpl(tvDiscoveryManager)
    }

    @Provides
    @Singleton
    fun providePairingRepository(
        application: Application,
        androidRemoteTv: AndroidRemoteTv
    ): PairingRepository {
        return PairingRepositoryImpl(application, androidRemoteTv)
    }

    @Provides
    @Singleton
    fun provideRemoteRepository(
        androidRemoteTv: AndroidRemoteTv
    ): RemoteRepository {
        return RemoteRepositoryImpl(androidRemoteTv)
    }
}