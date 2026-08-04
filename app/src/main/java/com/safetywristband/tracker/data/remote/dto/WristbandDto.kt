package com.safetywristband.tracker.data.remote.dto

import com.google.firebase.database.IgnoreExtraProperties
import com.safetywristband.tracker.domain.model.MovementStatus
import com.safetywristband.tracker.domain.model.WristbandData

@IgnoreExtraProperties
data class WristbandDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val accuracy: Double? = null,
    val speed: Double? = null,
    val movement: String? = null,
    val fallDetected: Boolean? = null,
    val bandRemoved: Boolean? = null,
    val sos: Boolean? = null,
    val timestamp: Long? = null
) {
    fun toDomain(wristbandId: String): WristbandData = WristbandData(
        wristbandId = wristbandId,
        latitude = latitude ?: 0.0,
        longitude = longitude ?: 0.0,
        altitude = altitude ?: 0.0,
        accuracy = accuracy ?: 0.0,
        speed = speed ?: 0.0,
        movement = MovementStatus.fromRaw(movement),
        fallDetected = fallDetected ?: false,
        bandRemoved = bandRemoved ?: false,
        sos = sos ?: false,
        timestamp = timestamp ?: 0L
    )
}
