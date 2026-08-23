package com.telecommande.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.telecommande.core.AndroidRemoteContext
import com.telecommande.core.AndroidRemoteTv
import com.telecommande.core.discovery.TvDiscoveryManager
import com.telecommande.data.dao.PairedTvDao
import com.telecommande.data.database.AppDatabase
import com.telecommande.data.repository.appSettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAndroidRemoteContext(application: Application): AndroidRemoteContext {
        return AndroidRemoteContext.getInstance(application)
    }

    @Provides
    @Singleton
    fun provideAndroidRemoteTv(
    ): AndroidRemoteTv {
        return AndroidRemoteTv()
    }

    @Provides
    @Singleton
    fun provideTvDiscoveryManager(
        application: Application
    ): TvDiscoveryManager {
        return TvDiscoveryManager(application)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return AppDatabase.getDatabase(appContext)
    }

    @Provides
    @Singleton
    fun providePairedTvDao(appDatabase: AppDatabase): PairedTvDao {
        return appDatabase.pairedTvDao()
    }

    @Provides
    @Singleton
    fun provideAppSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.appSettingsDataStore
    }
}

