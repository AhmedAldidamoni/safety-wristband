package com.safetywristband.tracker.presentation.map

import com.safetywristband.tracker.domain.model.ConnectionStatus
import com.safetywristband.tracker.domain.model.WristbandData

data class LiveMapUiState(
    val isLoading: Boolean = true,
    val wristbandData: WristbandData? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val autoFollow: Boolean = true,
    val errorMessage: String? = null
)
