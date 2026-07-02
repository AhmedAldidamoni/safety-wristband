package com.safewristband.tracker.domain.usecase

import com.safewristband.tracker.domain.model.GeofenceConfig
import com.safewristband.tracker.util.DistanceCalculator
import javax.inject.Inject

class CheckGeofenceStatusUseCase @Inject constructor() {
    operator fun invoke(
        currentLat: Double,
        currentLon: Double,
        config: GeofenceConfig
    ): GeofenceStatusResult {
        if (!config.isConfigured) return GeofenceStatusResult.NotConfigured

        val distance = DistanceCalculator.haversineDistanceMeters(
            currentLat, currentLon, config.centerLatitude, config.centerLongitude
        )
        return if (distance <= config.radiusMeters) {
            GeofenceStatusResult.Inside(distance)
        } else {
            GeofenceStatusResult.Outside(distance)
        }
    }
}

sealed class GeofenceStatusResult {
    data object NotConfigured : GeofenceStatusResult()
    data class Inside(val distanceMeters: Double) : GeofenceStatusResult()
    data class Outside(val distanceMeters: Double) : GeofenceStatusResult()
}
