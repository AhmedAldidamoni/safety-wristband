package com.safewristband.tracker.domain.usecase

import com.safewristband.tracker.domain.model.GeofenceConfig
import com.safewristband.tracker.domain.repository.GeofenceRepository
import javax.inject.Inject

class SaveGeofenceUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    suspend operator fun invoke(config: GeofenceConfig) =
        geofenceRepository.saveGeofenceConfig(config)
}
