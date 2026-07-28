package com.safewristband.tracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.AlertType
import com.safewristband.tracker.domain.model.WristbandData
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
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
            observeSettingsUseCase().distinctUntilChanged { old, new ->
                old.selectedWristbandId == new.selectedWristbandId
            }.collect { settings ->
                observeData(settings.selectedWristbandId)
                observeConnectionState(settings.selectedWristbandId)
                observeAlerts()
            }
        }
    }

    private fun observeData(wristbandId: String) = viewModelScope.launch {
        observeWristbandDataUseCase(wristbandId).collect { dataResource ->
            when (dataResource) {
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = dataResource.message
                    )
                }
                is Resource.Success -> _uiState.update {
                    checkForNewAlerts(dataResource.data)
                    it.copy(
                        isLoading = false,
                        wristbandData = dataResource.data,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun observeConnectionState(wristbandId: String) = viewModelScope.launch {
        observeConnectionStatusUseCase(wristbandId).collect { connectionStatus ->
            _uiState.update {
                it.copy(connectionStatus = connectionStatus)
            }
        }
    }

    private fun observeAlerts() = viewModelScope.launch {
        observeAlertsUseCase().collect { alerts ->
            _uiState.update {
                it.copy(activeAlerts = alerts)
            }
        }
    }

    private fun checkForNewAlerts(data: WristbandData) {
        viewModelScope.launch {
            if (data.sos && !previousSos) addAlertUseCase(
                AlertEvent(
                    type = AlertType.SOS_ACTIVATED,
                    message = "SOS activated",
                    timestamp = System.currentTimeMillis(),
                    latitude = data.latitude,
                    longitude = data.longitude
                )
            )
            if (data.fallDetected && !previousFallDetected) addAlertUseCase(
                AlertEvent(
                    type = AlertType.FALL_DETECTED,
                    message = "Fall detected",
                    timestamp = System.currentTimeMillis(),
                    latitude = data.latitude,
                    longitude = data.longitude
                )
            )
            if (data.bandRemoved && !previousBandRemoved) addAlertUseCase(
                AlertEvent(
                    type = AlertType.WRISTBAND_REMOVED,
                    message = "Wristband removed",
                    timestamp = System.currentTimeMillis(),
                    latitude = data.latitude,
                    longitude = data.longitude
                )
            )

            previousSos = data.sos
            previousFallDetected = data.fallDetected
            previousBandRemoved = data.bandRemoved
        }
    }
}
