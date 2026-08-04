package com.safetywristband.tracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.database.FirebaseDatabase
import com.safetywristband.tracker.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeofenceDataStore

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_SETTINGS
)

private val Context.geofenceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_GEOFENCE
)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    @SettingsDataStore
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    @Singleton
    @GeofenceDataStore
    fun provideGeofenceDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.geofenceDataStore
}
