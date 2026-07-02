package com.safewristband.tracker.domain.model

data class WristbandData(
    val wristbandId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Double = 0.0,
    val speed: Double = 0.0,
    val movement: MovementStatus = MovementStatus.UNKNOWN,
    val fallDetected: Boolean = false,
    val bandRemoved: Boolean = false,
    val sos: Boolean = false,
    val timestamp: Long = 0L
)

enum class MovementStatus {
    STATIONARY,
    WALKING,
    RUNNING,
    UNKNOWN;

    companion object {
        fun fromRaw(raw: String?): MovementStatus =
            entries.find { it.name.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}
