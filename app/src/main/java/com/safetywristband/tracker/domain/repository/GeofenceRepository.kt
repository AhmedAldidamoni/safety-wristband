package com.safetywristband.tracker.domain.repository

import com.safetywristband.tracker.domain.model.GeofenceConfig
import kotlinx.coroutines.flow.Flow

interface GeofenceRepository {
    fun observeGeofenceConfig(): Flow<GeofenceConfig>
    suspend fun saveGeofenceConfig(config: GeofenceConfig)
    suspend fun clearGeofenceConfig()
}
