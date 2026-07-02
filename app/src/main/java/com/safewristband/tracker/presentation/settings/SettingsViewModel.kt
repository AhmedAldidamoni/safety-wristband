package com.safewristband.tracker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safewristband.tracker.domain.model.AppSettings
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.ThemeMode
import com.safewristband.tracker.domain.usecase.ClearAlertHistoryUseCase
import com.safewristband.tracker.domain.usecase.ObserveConnectionStatusUseCase
import com.safewristband.tracker.domain.usecase.ObserveSettingsUseCase
import com.safewristband.tracker.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING,
    val historyCleared: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val observeConnectionStatusUseCase: ObserveConnectionStatusUseCase,
    private val clearAlertHistoryUseCase: ClearAlertHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
        viewModelScope.launch {
            observeSettingsUseCase()
                .distinctUntilChanged { old, new -> old.selectedWristbandId == new.selectedWristbandId }
                .flatMapLatest { settings -> observeConnectionStatusUseCase(settings.selectedWristbandId) }
                .collect { status -> _uiState.value = _uiState.value.copy(connectionStatus = status) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun clearAlertHistory() {
        viewModelScope.launch {
            clearAlertHistoryUseCase()
            _uiState.value = _uiState.value.copy(historyCleared = true)
        }
    }

    fun acknowledgeHistoryCleared() {
        _uiState.value = _uiState.value.copy(historyCleared = false)
    }
}
