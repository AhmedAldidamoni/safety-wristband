package com.safewristband.tracker.data.repository

import com.safewristband.tracker.data.local.GeofencePreferences
import com.safewristband.tracker.domain.model.GeofenceConfig
import com.safewristband.tracker.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepositoryImpl @Inject constructor(
    private val geofencePreferences: GeofencePreferences
) : GeofenceRepository {

    override fun observeGeofenceConfig(): Flow<GeofenceConfig> =
        geofencePreferences.geofenceConfig

    override suspend fun saveGeofenceConfig(config: GeofenceConfig) {
        geofencePreferences.save(config)
    }

    override suspend fun clearGeofenceConfig() {
        geofencePreferences.clear()
    }
}
