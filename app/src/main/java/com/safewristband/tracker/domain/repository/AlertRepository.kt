package com.safewristband.tracker.domain.repository

import com.safewristband.tracker.domain.model.AlertEvent
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeAlerts(): Flow<List<AlertEvent>>
    suspend fun addAlert(alert: AlertEvent)
    suspend fun clearHistory()
}
