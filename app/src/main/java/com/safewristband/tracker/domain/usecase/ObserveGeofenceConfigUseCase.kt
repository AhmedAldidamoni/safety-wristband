package com.safewristband.tracker.domain.usecase

import com.safewristband.tracker.domain.model.GeofenceConfig
import com.safewristband.tracker.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGeofenceConfigUseCase @Inject constructor(
    private val geofenceRepository: GeofenceRepository
) {
    operator fun invoke(): Flow<GeofenceConfig> = geofenceRepository.observeGeofenceConfig()
}
