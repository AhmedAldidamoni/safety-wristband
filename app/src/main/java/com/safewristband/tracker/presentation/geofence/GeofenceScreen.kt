package com.safewristband.tracker.presentation.geofence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.safewristband.tracker.domain.usecase.GeofenceStatusResult
import com.safewristband.tracker.presentation.theme.AlertRed
import com.safewristband.tracker.presentation.theme.OkGreen
import com.safewristband.tracker.util.Constants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(viewModel: GeofenceViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Geofence") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                val defaultCenter = LatLng(
                    uiState.pendingCenterLat ?: uiState.wristbandData?.latitude ?: 0.0,
                    uiState.pendingCenterLon ?: uiState.wristbandData?.longitude ?: 0.0
                )
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(defaultCenter, 15f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> viewModel.onMapTapped(latLng.latitude, latLng.longitude) }
                ) {
                    if (uiState.pendingCenterLat != null && uiState.pendingCenterLon != null) {
                        val center = LatLng(uiState.pendingCenterLat!!, uiState.pendingCenterLon!!)
                        Circle(
                            center = center,
                            radius = uiState.pendingRadiusMeters,
                            fillColor = Color(0x330F766E),
                            strokeColor = Color(0xFF0F766E),
                            strokeWidth = 3f
                        )
                        Marker(state = MarkerState(position = center), title = "Safe zone center")
                    }
                    uiState.wristbandData?.let { data ->
                        Marker(
                            state = MarkerState(position = LatLng(data.latitude, data.longitude)),
                            title = "Wristband"
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tap the map to set the safe zone center", style = MaterialTheme.typography.bodyMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Radius", style = MaterialTheme.typography.titleMedium)
                        Text(
                            String.format(Locale.US, "%.0f m", uiState.pendingRadiusMeters),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Slider(
                        value = uiState.pendingRadiusMeters.toFloat(),
                        onValueChange = { viewModel.onRadiusChanged(it.toDouble()) },
                        valueRange = Constants.MIN_GEOFENCE_RADIUS_METERS.toFloat()..Constants.MAX_GEOFENCE_RADIUS_METERS.toFloat()
                    )

                    GeofenceStatusBanner(statusResult = uiState.statusResult)

                    Button(
                        onClick = viewModel::saveGeofence,
                        enabled = uiState.pendingCenterLat != null,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) {
                        Text(if (uiState.saveSuccess) "Saved" else "Save Geofence")
                    }
                }
            }
        }
    }
}

@Composable
private fun GeofenceStatusBanner(statusResult: GeofenceStatusResult) {
    val (label, color) = when (statusResult) {
        is GeofenceStatusResult.NotConfigured -> "No geofence configured yet" to MaterialTheme.colorScheme.onSurfaceVariant
        is GeofenceStatusResult.Inside -> String.format(Locale.US, "Inside safe zone (%.0f m from center)", statusResult.distanceMeters) to OkGreen
        is GeofenceStatusResult.Outside -> String.format(Locale.US, "Outside safe zone (%.0f m from center)", statusResult.distanceMeters) to AlertRed
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
