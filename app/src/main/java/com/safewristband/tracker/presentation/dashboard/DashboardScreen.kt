package com.safewristband.tracker.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.MovementStatus
import com.safewristband.tracker.presentation.components.ErrorView
import com.safewristband.tracker.presentation.components.LoadingIndicator
import com.safewristband.tracker.presentation.components.StatusCard
import com.safewristband.tracker.presentation.theme.AlertRed
import com.safewristband.tracker.presentation.theme.OkGreen
import com.safewristband.tracker.presentation.theme.WarningAmber
import com.safewristband.tracker.util.DateTimeUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.wristbandData == null -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.errorMessage != null && uiState.wristbandData == null -> ErrorView(
                message = uiState.errorMessage ?: "Something went wrong",
                modifier = Modifier.padding(padding)
            )
            else -> DashboardContent(uiState = uiState, padding = padding)
        }
    }
}

@Composable
private fun DashboardContent(uiState: DashboardUiState, padding: PaddingValues) {
    val data = uiState.wristbandData

    val connectionColor = when (uiState.connectionStatus) {
        ConnectionStatus.CONNECTED -> OkGreen
        ConnectionStatus.STALE -> WarningAmber
        ConnectionStatus.DISCONNECTED -> AlertRed
        ConnectionStatus.CONNECTING -> WarningAmber
    }

    val cards = buildList {
        add(
            CardSpec(
                title = "Connection",
                value = uiState.connectionStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                icon = Icons.Filled.Watch,
                color = connectionColor,
                pulsing = uiState.connectionStatus != ConnectionStatus.CONNECTED,
                subtitle = data?.let { "Last update: ${DateTimeUtils.timeAgo(it.timestamp)}" }
            )
        )
        add(
            CardSpec(
                title = "Location",
                value = data?.let { String.format(Locale.US, "%.5f, %.5f", it.latitude, it.longitude) } ?: "--",
                icon = Icons.Filled.MyLocation,
                color = MaterialTheme.colorScheme.primary
            )
        )
        add(
            CardSpec(
                title = "Altitude",
                value = data?.let { String.format(Locale.US, "%.1f m", it.altitude) } ?: "--",
                icon = Icons.Filled.Height,
                color = MaterialTheme.colorScheme.primary
            )
        )
        add(
            CardSpec(
                title = "GPS Accuracy",
                value = data?.let { String.format(Locale.US, "±%.1f m", it.accuracy) } ?: "--",
                icon = Icons.Filled.Sensors,
                color = MaterialTheme.colorScheme.primary
            )
        )
        add(
            CardSpec(
                title = "Speed",
                value = data?.let { String.format(Locale.US, "%.1f m/s", it.speed) } ?: "--",
                icon = Icons.Filled.Speed,
                color = MaterialTheme.colorScheme.primary
            )
        )
        add(
            CardSpec(
                title = "Movement",
                value = data?.movement?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                icon = Icons.Filled.SportsScore,
                color = if (data?.movement == MovementStatus.RUNNING) WarningAmber else MaterialTheme.colorScheme.primary
            )
        )
        add(
            CardSpec(
                title = "Fall Detection",
                value = if (data?.fallDetected == true) "Fall Detected" else "Normal",
                icon = Icons.Filled.Warning,
                color = if (data?.fallDetected == true) AlertRed else OkGreen,
                pulsing = data?.fallDetected == true
            )
        )
        add(
            CardSpec(
                title = "SOS Status",
                value = if (data?.sos == true) "SOS ACTIVE" else "Normal",
                icon = Icons.Filled.NotificationsActive,
                color = if (data?.sos == true) AlertRed else OkGreen,
                pulsing = data?.sos == true
            )
        )
        add(
            CardSpec(
                title = "Band Removal",
                value = if (data?.bandRemoved == true) "Removed" else "Worn",
                icon = Icons.Filled.PersonOff,
                color = if (data?.bandRemoved == true) AlertRed else OkGreen,
                pulsing = data?.bandRemoved == true
            )
        )
        add(
            CardSpec(
                title = "Active Alerts",
                value = "${uiState.activeAlerts.size}",
                icon = Icons.Filled.NotificationsActive,
                color = if (uiState.activeAlerts.isNotEmpty()) WarningAmber else OkGreen
            )
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(padding)
    ) {
        items(cards) { card ->
            StatusCard(
                title = card.title,
                value = card.value,
                icon = card.icon,
                accentColor = card.color,
                isPulsing = card.pulsing,
                subtitle = card.subtitle
            )
        }
    }
}

private data class CardSpec(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val pulsing: Boolean = false,
    val subtitle: String? = null
)
