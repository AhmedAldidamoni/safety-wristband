package com.safetywristband.tracker.presentation.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safetywristband.tracker.R
import com.safetywristband.tracker.domain.model.AlertEvent
import com.safetywristband.tracker.domain.model.AlertType
import com.safetywristband.tracker.presentation.components.LoadingIndicator
import com.safetywristband.tracker.presentation.theme.AlertRed
import com.safetywristband.tracker.presentation.theme.OkGreen
import com.safetywristband.tracker.util.DateTimeUtils

@Composable
fun AlertsHistoryScreen(viewModel: AlertsHistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AlertsHeader(alertsCount = uiState.alerts.size, onClear = viewModel::clearHistory) },
        contentWindowInsets = WindowInsets(bottom = 0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.alerts.isEmpty() -> EmptyState(modifier = Modifier.align(Alignment.Center))

                else -> AnimatedVisibility(
                    visibleState = listState,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.alerts,
                            key = { _, alert -> alert.id }
                        ) { index, alert ->
                            val visible = remember {
                                MutableTransitionState(false).apply { targetState = true }
                            }

                            AnimatedVisibility(
                                visibleState = visible,
                                enter = fadeIn(tween(350, delayMillis = index * 60)) +
                                        slideInHorizontally(tween(350, delayMillis = index * 60)) { it / 3 }
                            ) {
                                AlertCard(alert = alert)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Header ───

@Composable
private fun AlertsHeader(alertsCount: Int, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = stringResource(R.string.clear_history),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.alerts_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                if (alertsCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(AlertRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = alertsCount.coerceAtMost(99).toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = OkGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.alerts_subtitle, alertsCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 6.dp)
        )
    }
}

// ─── Alert Card ───

@Composable
private fun AlertCard(alert: AlertEvent, modifier: Modifier = Modifier) {
    val theme = alertTheme(alert.type)

    val bgAlpha = 0.15f
    val borderAlpha = 0.50f

    // Critical alerts pulse
    val isCritical = alert.type == AlertType.SOS_ACTIVATED || alert.type == AlertType.FALL_DETECTED
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = borderAlpha,
        targetValue = if (isCritical) borderAlpha + 0.15f else borderAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = theme.accentColor.copy(alpha = bgAlpha)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            theme.accentColor.copy(alpha = if (isCritical) pulseAlpha else borderAlpha)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Status dot + Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Glowing status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(theme.accentColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = alert.message,
                        color = theme.accentColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = DateTimeUtils.formatDateTime(alert.timestamp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            // Right: Icon in tinted circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(theme.accentColor.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = theme.icon,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_alerts_yet),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

// ─── Theme Mapping ───

private data class AlertTheme(
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
private fun alertTheme(type: AlertType): AlertTheme = when (type) {
    AlertType.SOS_ACTIVATED -> AlertTheme(
        Icons.Filled.NotificationsActive,
        Color(0xFFF43F5E)
    )
    AlertType.FALL_DETECTED -> AlertTheme(
        Icons.Filled.Warning,
        Color(0xFF8B5CF6)
    )
    AlertType.WRISTBAND_REMOVED -> AlertTheme(
        Icons.Filled.PersonOff,
        Color(0xFFF59E0B)
    )
    AlertType.GEOFENCE_EXITED -> AlertTheme(
        Icons.Filled.LocationOff,
        Color(0xFF3B82F6)
    )
    AlertType.GEOFENCE_ENTERED -> AlertTheme(
        Icons.Filled.LocationOn,
        Color(0xFF10B981)
    )
    AlertType.CONNECTION_LOST -> AlertTheme(
        Icons.Filled.CloudOff,
        Color(0xFFF59E0B)
    )
    AlertType.CONNECTION_RESTORED -> AlertTheme(
        Icons.AutoMirrored.Filled.DirectionsWalk,
        Color(0xFF06B6D4)
    )
}