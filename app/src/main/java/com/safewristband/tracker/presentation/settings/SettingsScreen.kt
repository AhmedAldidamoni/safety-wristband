package com.safewristband.tracker.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.ThemeMode
import com.safewristband.tracker.presentation.theme.AlertRed
import com.safewristband.tracker.presentation.theme.OkGreen
import com.safewristband.tracker.presentation.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = "Firebase Connection") {
                val (label, color) = when (uiState.connectionStatus) {
                    ConnectionStatus.CONNECTED -> "Connected" to OkGreen
                    ConnectionStatus.STALE -> "Stale data" to WarningAmber
                    ConnectionStatus.DISCONNECTED -> "Disconnected" to AlertRed
                    ConnectionStatus.CONNECTING -> "Connecting..." to WarningAmber
                }
                Text(label, style = MaterialTheme.typography.bodyLarge, color = color)
                Text(
                    "Wristband ID: ${uiState.settings.selectedWristbandId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard(title = "Theme") {
                Column(Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.settings.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = uiState.settings.themeMode == mode, onClick = null)
                            Text(
                                mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            SectionCard(title = "Application Info") {
                Text("SafeWristband v1.0.0", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Smart safety wristband monitoring companion app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard(title = "Alert History") {
                Text(
                    if (uiState.historyCleared) "History cleared" else "Remove all locally stored alert events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = viewModel::clearAlertHistory,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Clear Local Alert History")
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
    }
}
