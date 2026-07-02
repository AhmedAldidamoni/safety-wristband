package com.safewristband.tracker.presentation.dashboard

import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.WristbandData

data class DashboardUiState(
    val isLoading: Boolean = true,
    val wristbandData: WristbandData? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val activeAlerts: List<AlertEvent> = emptyList(),
    val errorMessage: String? = null
)
