package com.safewristband.tracker.domain.usecase

import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.repository.WristbandRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConnectionStatusUseCase @Inject constructor(
    private val wristbandRepository: WristbandRepository
) {
    operator fun invoke(wristbandId: String): Flow<ConnectionStatus> =
        wristbandRepository.observeConnectionStatus(wristbandId)
}
