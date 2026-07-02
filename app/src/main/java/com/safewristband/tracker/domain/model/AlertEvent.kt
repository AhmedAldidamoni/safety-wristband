package com.safewristband.tracker.domain.model

data class AlertEvent(
    val id: Long = 0L,
    val type: AlertType,
    val message: String,
    val timestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)

enum class AlertType {
    SOS_ACTIVATED,
    FALL_DETECTED,
    WRISTBAND_REMOVED,
    GEOFENCE_EXITED,
    GEOFENCE_ENTERED,
    CONNECTION_LOST,
    CONNECTION_RESTORED
}
