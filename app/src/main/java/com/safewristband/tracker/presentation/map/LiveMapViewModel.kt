package com.safewristband.tracker.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class LiveMapViewModel @Inject constructor(
    private val observeWristbandDataUseCase: ObserveWristbandDataUseCase,
    private val observeConnectionStatusUseCase: ObserveConnectionStatusUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveMapUiState())
    val uiState: StateFlow<LiveMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettingsUseCase().distinctUntilChanged { old, new -> old.selectedWristbandId == new.selectedWristbandId }
                .collect { settings -> observeData(settings.selectedWristbandId) }
        }
    }

    private fun observeData(wristbandId: String) {
        viewModelScope.launch {
            combine(
                observeWristbandDataUseCase(wristbandId),
                observeConnectionStatusUseCase(wristbandId)
            ) { dataResource, connectionStatus -> dataResource to connectionStatus }
                .collect { (dataResource, connectionStatus) ->
                    when (dataResource) {
                        is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                        is Resource.Error -> _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = dataResource.message,
                            connectionStatus = connectionStatus
                        )
                        is Resource.Success -> _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            wristbandData = dataResource.data,
                            connectionStatus = connectionStatus,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun toggleAutoFollow() {
        _uiState.value = _uiState.value.copy(autoFollow = !_uiState.value.autoFollow)
    }
}
