package com.safewristband.tracker.domain.usecase

import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAlertsUseCase @Inject constructor(
    private val alertRepository: AlertRepository
) {
    operator fun invoke(): Flow<List<AlertEvent>> = alertRepository.observeAlerts()
}

class AddAlertUseCase @Inject constructor(
    private val alertRepository: AlertRepository
) {
    suspend operator fun invoke(alert: AlertEvent) = alertRepository.addAlert(alert)
}

class ClearAlertHistoryUseCase @Inject constructor(
    private val alertRepository: AlertRepository
) {
    suspend operator fun invoke() = alertRepository.clearHistory()
}
