package com.safetywristband.tracker.presentation.geofence

import com.safetywristband.tracker.domain.model.GeofenceConfig
import com.safetywristband.tracker.domain.model.WristbandData
import com.safetywristband.tracker.domain.usecase.GeofenceStatusResult

data class GeofenceUiState(
    val isLoading: Boolean = true,
    val savedConfig: GeofenceConfig = GeofenceConfig(),
    val pendingCenterLat: Double? = null,
    val pendingCenterLon: Double? = null,
    val pendingRadiusMeters: Double = 100.0,
    val wristbandData: WristbandData? = null,
    val statusResult: GeofenceStatusResult = GeofenceStatusResult.NotConfigured,
    val saveSuccess: Boolean = false
)
