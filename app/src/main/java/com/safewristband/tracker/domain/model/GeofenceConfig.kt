package com.safewristband.tracker.domain.model

data class GeofenceConfig(
    val isConfigured: Boolean = false,
    val centerLatitude: Double = 0.0,
    val centerLongitude: Double = 0.0,
    val radiusMeters: Double = 100.0
)
