package com.safewristband.tracker.domain.usecase

import com.safewristband.tracker.domain.model.WristbandData
import com.safewristband.tracker.domain.repository.WristbandRepository
import com.safewristband.tracker.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWristbandDataUseCase @Inject constructor(
    private val wristbandRepository: WristbandRepository
) {
    operator fun invoke(wristbandId: String): Flow<Resource<WristbandData>> =
        wristbandRepository.observeWristbandData(wristbandId)
}
