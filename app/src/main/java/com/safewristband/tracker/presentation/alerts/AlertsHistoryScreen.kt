package com.safewristband.tracker.presentation.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.AlertType
import com.safewristband.tracker.presentation.components.LoadingIndicator
import com.safewristband.tracker.presentation.theme.AlertRed
import com.safewristband.tracker.presentation.theme.OkGreen
import com.safewristband.tracker.presentation.theme.WarningAmber
import com.safewristband.tracker.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsHistoryScreen(viewModel: AlertsHistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts History") },
                actions = {
                    IconButton(onClick = viewModel::clearHistory) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.alerts.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No alerts yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.alerts, key = { it.id }) { alert ->
                    AlertRow(alert)
                }
            }
        }
    }
}

@Composable
private fun AlertRow(alert: AlertEvent) {
    val (icon, color) = alertVisuals(alert.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
                Text(alert.message, style = MaterialTheme.typography.titleMedium)
                Text(
                    DateTimeUtils.formatDateTime(alert.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun alertVisuals(type: AlertType): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (type) {
    AlertType.SOS_ACTIVATED -> Icons.Filled.NotificationsActive to AlertRed
    AlertType.FALL_DETECTED -> Icons.Filled.Warning to AlertRed
    AlertType.WRISTBAND_REMOVED -> Icons.Filled.PersonOff to WarningAmber
    AlertType.GEOFENCE_EXITED -> Icons.Filled.LocationOff to WarningAmber
    AlertType.GEOFENCE_ENTERED -> Icons.Filled.LocationOn to OkGreen
    AlertType.CONNECTION_LOST -> Icons.Filled.CloudOff to WarningAmber
    AlertType.CONNECTION_RESTORED -> Icons.Filled.DirectionsWalk to OkGreen
}
