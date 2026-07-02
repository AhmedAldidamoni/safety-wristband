package com.safewristband.tracker.domain.repository

import com.safewristband.tracker.domain.model.GeofenceConfig
import kotlinx.coroutines.flow.Flow

interface GeofenceRepository {
    fun observeGeofenceConfig(): Flow<GeofenceConfig>
    suspend fun saveGeofenceConfig(config: GeofenceConfig)
    suspend fun clearGeofenceConfig()
}
