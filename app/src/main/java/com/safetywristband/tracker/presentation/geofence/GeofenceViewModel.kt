package com.safetywristband.tracker.presentation.geofence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safetywristband.tracker.domain.model.AlertEvent
import com.safetywristband.tracker.domain.model.AlertType
import com.safetywristband.tracker.domain.model.GeofenceConfig
import com.safetywristband.tracker.domain.usecase.AddAlertUseCase
import com.safetywristband.tracker.domain.usecase.CheckGeofenceStatusUseCase
import com.safetywristband.tracker.domain.usecase.GeofenceStatusResult
import com.safetywristband.tracker.domain.usecase.ObserveGeofenceConfigUseCase
import com.safetywristband.tracker.domain.usecase.ObserveSettingsUseCase
import com.safetywristband.tracker.domain.usecase.ObserveWristbandDataUseCase
import com.safetywristband.tracker.domain.usecase.SaveGeofenceUseCase
import com.safetywristband.tracker.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeofenceViewModel @Inject constructor(
    private val observeGeofenceConfigUseCase: ObserveGeofenceConfigUseCase,
    private val saveGeofenceUseCase: SaveGeofenceUseCase,
    private val observeWristbandDataUseCase: ObserveWristbandDataUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val checkGeofenceStatusUseCase: CheckGeofenceStatusUseCase,
    private val addAlertUseCase: AddAlertUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeofenceUiState())
    val uiState: StateFlow<GeofenceUiState> = _uiState.asStateFlow()

    private var wasInside: Boolean? = null

    init {
        viewModelScope.launch {
            observeGeofenceConfigUseCase().collect { config ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    savedConfig = config,
                    pendingCenterLat = config.centerLatitude.takeIf { config.isConfigured },
                    pendingCenterLon = config.centerLongitude.takeIf { config.isConfigured },
                    pendingRadiusMeters = if (config.isConfigured) config.radiusMeters else _uiState.value.pendingRadiusMeters
                )
            }
        }

        viewModelScope.launch {
            observeSettingsUseCase().distinctUntilChanged { old, new -> old.selectedWristbandId == new.selectedWristbandId }
                .collect { settings -> observeWristbandLocation(settings.selectedWristbandId) }
        }
    }

    private fun observeWristbandLocation(wristbandId: String) {
        viewModelScope.launch {
            observeWristbandDataUseCase(wristbandId).collect { resource ->
                if (resource is Resource.Success) {
                    val data = resource.data
                    _uiState.value = _uiState.value.copy(wristbandData = data)
                    val result = checkGeofenceStatusUseCase(data.latitude, data.longitude, _uiState.value.savedConfig)
                    _uiState.value = _uiState.value.copy(statusResult = result)
                    handleGeofenceTransition(result, data.latitude, data.longitude)
                }
            }
        }
    }

    private fun handleGeofenceTransition(result: GeofenceStatusResult, lat: Double, lon: Double) {
        val isInside = result is GeofenceStatusResult.Inside
        if (result !is GeofenceStatusResult.NotConfigured) {
            if (wasInside == true && !isInside) {
                viewModelScope.launch {
                    addAlertUseCase(
                        AlertEvent(
                            type = AlertType.GEOFENCE_EXITED,
                            message = "Wristband exited the safe zone",
                            timestamp = System.currentTimeMillis(),
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }
            } else if (wasInside == false && isInside) {
                viewModelScope.launch {
                    addAlertUseCase(
                        AlertEvent(
                            type = AlertType.GEOFENCE_ENTERED,
                            message = "Wristband re-entered the safe zone",
                            timestamp = System.currentTimeMillis(),
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }
            }
            wasInside = isInside
        }
    }

    fun onMapTapped(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(pendingCenterLat = lat, pendingCenterLon = lon, saveSuccess = false)
    }

    fun onRadiusChanged(radiusMeters: Double) {
        _uiState.value = _uiState.value.copy(pendingRadiusMeters = radiusMeters, saveSuccess = false)
    }

    fun saveGeofence() {
        val lat = _uiState.value.pendingCenterLat ?: return
        val lon = _uiState.value.pendingCenterLon ?: return
        viewModelScope.launch {
            saveGeofenceUseCase(
                GeofenceConfig(
                    isConfigured = true,
                    centerLatitude = lat,
                    centerLongitude = lon,
                    radiusMeters = _uiState.value.pendingRadiusMeters
                )
            )
            _uiState.value = _uiState.value.copy(saveSuccess = true)
        }
    }
}
