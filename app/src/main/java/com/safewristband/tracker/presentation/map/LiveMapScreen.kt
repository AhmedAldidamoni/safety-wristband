package com.safewristband.tracker.presentation.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.safewristband.tracker.presentation.components.ErrorView
import com.safewristband.tracker.presentation.components.LoadingIndicator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMapScreen(viewModel: LiveMapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Live Map") }) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.wristbandData == null -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.errorMessage != null && uiState.wristbandData == null -> ErrorView(
                message = uiState.errorMessage ?: "Unable to load location",
                modifier = Modifier.padding(padding)
            )
            else -> LiveMapContent(
                uiState = uiState,
                padding = padding,
                onToggleAutoFollow = viewModel::toggleAutoFollow
            )
        }
    }
}

@Composable
private fun LiveMapContent(
    uiState: LiveMapUiState,
    padding: PaddingValues,
    onToggleAutoFollow: () -> Unit
) {
    val data = uiState.wristbandData
    val position = LatLng(data?.latitude ?: 0.0, data?.longitude ?: 0.0)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 16f)
    }

    LaunchedEffect(position, uiState.autoFollow) {
        if (uiState.autoFollow) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(position, cameraPositionState.position.zoom)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            if (data != null) {
                Marker(
                    state = MarkerState(position = position),
                    title = "Wristband"
                )
            }
        }

        FloatingActionButton(
            onClick = onToggleAutoFollow,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Toggle auto-follow")
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-follow wristband", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = uiState.autoFollow, onCheckedChange = { onToggleAutoFollow() })
                }
                if (data != null) {
                    InfoRow("Latitude", String.format(Locale.US, "%.6f", data.latitude))
                    InfoRow("Longitude", String.format(Locale.US, "%.6f", data.longitude))
                    InfoRow("Altitude", String.format(Locale.US, "%.1f m", data.altitude))
                    InfoRow("GPS Accuracy", String.format(Locale.US, "±%.1f m", data.accuracy))
                    InfoRow("Speed", String.format(Locale.US, "%.1f m/s", data.speed))
                    InfoRow("Movement", data.movement.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
