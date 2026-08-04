package com.safetywristband.tracker.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.safetywristband.tracker.data.local.GeofencePreferences
import com.safetywristband.tracker.data.local.SettingsPreferences
import com.safetywristband.tracker.data.repository.AlertRepositoryImpl
import com.safetywristband.tracker.data.repository.GeofenceRepositoryImpl
import com.safetywristband.tracker.data.repository.SettingsRepositoryImpl
import com.safetywristband.tracker.data.repository.WristbandRepositoryImpl
import com.safetywristband.tracker.domain.repository.AlertRepository
import com.safetywristband.tracker.domain.repository.GeofenceRepository
import com.safetywristband.tracker.domain.repository.SettingsRepository
import com.safetywristband.tracker.domain.repository.WristbandRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWristbandRepository(impl: WristbandRepositoryImpl): WristbandRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(impl: GeofenceRepositoryImpl): GeofenceRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideGeofencePreferences(
            @GeofenceDataStore dataStore: DataStore<Preferences>
        ): GeofencePreferences = GeofencePreferences(dataStore)

        @Provides
        @Singleton
        fun provideSettingsPreferences(
            @SettingsDataStore dataStore: DataStore<Preferences>
        ): SettingsPreferences = SettingsPreferences(dataStore)
    }
}
