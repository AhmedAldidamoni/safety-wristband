package com.safewristband.tracker.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.usecase.ClearAlertHistoryUseCase
import com.safewristband.tracker.domain.usecase.ObserveAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsHistoryUiState(
    val alerts: List<AlertEvent> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AlertsHistoryViewModel @Inject constructor(
    private val observeAlertsUseCase: ObserveAlertsUseCase,
    private val clearAlertHistoryUseCase: ClearAlertHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsHistoryUiState())
    val uiState: StateFlow<AlertsHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAlertsUseCase().collect { alerts ->
                _uiState.value = AlertsHistoryUiState(alerts = alerts, isLoading = false)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { clearAlertHistoryUseCase() }
    }
}
