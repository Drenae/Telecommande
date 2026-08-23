package com.telecommande.di

import com.telecommande.data.repository.SettingsRepository
import com.telecommande.data.repository.SettingsRepositoryImpl
import com.telecommande.data.repository.discovery.DiscoveryRepository
import com.telecommande.data.repository.discovery.DiscoveryRepositoryImpl
import com.telecommande.data.repository.pairing.PairingRepository
import com.telecommande.data.repository.pairing.PairingRepositoryImpl
import com.telecommande.data.repository.remote.RemoteRepository
import com.telecommande.data.repository.remote.RemoteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(
        implementation: DiscoveryRepositoryImpl
    ): DiscoveryRepository

    @Binds
    @Singleton
    abstract fun bindPairingRepository(
        implementation: PairingRepositoryImpl
    ): PairingRepository

    @Binds
    @Singleton
    abstract fun bindRemoteRepository(
        implementation: RemoteRepositoryImpl
    ): RemoteRepository
}
