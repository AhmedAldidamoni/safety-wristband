package com.safetywristband.tracker.domain.usecase

import com.safetywristband.tracker.domain.model.GeofenceConfig
import com.safetywristband.tracker.domain.repository.GeofenceRepository
import javax.inject.Inject

class SaveGeofenceUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    suspend operator fun invoke(config: GeofenceConfig) =
        geofenceRepository.saveGeofenceConfig(config)
}
