package com.safewristband.tracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.AlertType
import com.safewristband.tracker.domain.usecase.AddAlertUseCase
import com.safewristband.tracker.domain.usecase.ObserveAlertsUseCase
import com.safewristband.tracker.domain.usecase.ObserveConnectionStatusUseCase
import com.safewristband.tracker.domain.usecase.ObserveSettingsUseCase
import com.safewristband.tracker.domain.usecase.ObserveWristbandDataUseCase
import com.safewristband.tracker.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeWristbandDataUseCase: ObserveWristbandDataUseCase,
    private val observeConnectionStatusUseCase: ObserveConnectionStatusUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeAlertsUseCase: ObserveAlertsUseCase,
    private val addAlertUseCase: AddAlertUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var previousFallDetected = false
    private var previousSos = false
    private var previousBandRemoved = false

    init {
        viewModelScope.launch {
            observeSettingsUseCase().distinctUntilChanged { old, new -> old.selectedWristbandId == new.selectedWristbandId }
                .collect { settings ->
                    observeData(settings.selectedWristbandId)
                }
        }
    }

    private fun observeData(wristbandId: String) {
        viewModelScope.launch {
            combine(
                observeWristbandDataUseCase(wristbandId),
                observeConnectionStatusUseCase(wristbandId),
                observeAlertsUseCase()
            ) { dataResource, connectionStatus, alerts ->
                Triple(dataResource, connectionStatus, alerts)
            }.collect { (dataResource, connectionStatus, alerts) ->
                when (dataResource) {
                    is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is Resource.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = dataResource.message,
                        connectionStatus = connectionStatus
                    )
                    is Resource.Success -> {
                        checkForNewAlerts(dataResource.data.fallDetected, dataResource.data.sos, dataResource.data.bandRemoved)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            wristbandData = dataResource.data,
                            connectionStatus = connectionStatus,
                            errorMessage = null,
                            activeAlerts = alerts.take(5)
                        )
                    }
                }
            }
        }
    }

    private fun checkForNewAlerts(fallDetected: Boolean, sos: Boolean, bandRemoved: Boolean) {
        viewModelScope.launch {
            if (sos && !previousSos) {
                addAlertUseCase(AlertEvent(type = AlertType.SOS_ACTIVATED, message = "SOS activated", timestamp = System.currentTimeMillis()))
            }
            if (fallDetected && !previousFallDetected) {
                addAlertUseCase(AlertEvent(type = AlertType.FALL_DETECTED, message = "Fall detected", timestamp = System.currentTimeMillis()))
            }
            if (bandRemoved && !previousBandRemoved) {
                addAlertUseCase(AlertEvent(type = AlertType.WRISTBAND_REMOVED, message = "Wristband removed", timestamp = System.currentTimeMillis()))
            }
            previousSos = sos
            previousFallDetected = fallDetected
            previousBandRemoved = bandRemoved
        }
    }
}
