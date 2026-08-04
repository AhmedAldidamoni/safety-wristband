package com.safetywristband.tracker.domain.usecase

import com.safetywristband.tracker.domain.model.WristbandData
import com.safetywristband.tracker.domain.repository.WristbandRepository
import com.safetywristband.tracker.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWristbandDataUseCase @Inject constructor(
    private val wristbandRepository: WristbandRepository
) {
    operator fun invoke(wristbandId: String): Flow<Resource<WristbandData>> =
        wristbandRepository.observeWristbandData(wristbandId)
}
