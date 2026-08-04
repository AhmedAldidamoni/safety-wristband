package com.safetywristband.tracker.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safetywristband.tracker.domain.model.AppSettings
import com.safetywristband.tracker.domain.model.ConnectionStatus
import com.safetywristband.tracker.domain.model.ThemeMode
import com.safetywristband.tracker.domain.usecase.ClearAlertHistoryUseCase
import com.safetywristband.tracker.domain.usecase.ObserveConnectionStatusUseCase
import com.safetywristband.tracker.domain.usecase.ObserveSettingsUseCase
import com.safetywristband.tracker.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
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
}
