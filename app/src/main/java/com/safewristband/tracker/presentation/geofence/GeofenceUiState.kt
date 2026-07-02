package com.safewristband.tracker.presentation.geofence

import com.safewristband.tracker.domain.model.GeofenceConfig
import com.safewristband.tracker.domain.model.WristbandData
import com.safewristband.tracker.domain.usecase.GeofenceStatusResult

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
