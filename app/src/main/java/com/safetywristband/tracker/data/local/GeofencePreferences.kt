package com.safetywristband.tracker.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.safetywristband.tracker.domain.model.GeofenceConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object GeofenceKeys {
    val IS_CONFIGURED = booleanPreferencesKey("is_configured")
    val CENTER_LAT = doublePreferencesKey("center_lat")
    val CENTER_LON = doublePreferencesKey("center_lon")
    val RADIUS = doublePreferencesKey("radius_meters")
}

@Singleton
class GeofencePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val geofenceConfig: Flow<GeofenceConfig> = dataStore.data.map { prefs ->
        GeofenceConfig(
            isConfigured = prefs[GeofenceKeys.IS_CONFIGURED] ?: false,
            centerLatitude = prefs[GeofenceKeys.CENTER_LAT] ?: 0.0,
            centerLongitude = prefs[GeofenceKeys.CENTER_LON] ?: 0.0,
            radiusMeters = prefs[GeofenceKeys.RADIUS] ?: 100.0
        )
    }

    suspend fun save(config: GeofenceConfig) {
        dataStore.edit { prefs ->
            prefs[GeofenceKeys.IS_CONFIGURED] = true
            prefs[GeofenceKeys.CENTER_LAT] = config.centerLatitude
            prefs[GeofenceKeys.CENTER_LON] = config.centerLongitude
            prefs[GeofenceKeys.RADIUS] = config.radiusMeters
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs[GeofenceKeys.IS_CONFIGURED] = false
        }
    }
}
