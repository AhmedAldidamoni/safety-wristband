package com.safewristband.tracker.presentation.alerts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.AlertType
import com.safewristband.tracker.presentation.components.LoadingIndicator
import com.safewristband.tracker.util.DateTimeUtils

// ─── Dark Theme Palette ───
private val DarkBackground = Color(0xFF0B1120)
private val TextPrimary = Color(0xFFE2E8F0)
private val TextSecondary = Color(0xFF8B95A5)
private val TextMuted = Color(0xFF5A6578)

private val OrangeAccent = Color(0xFFFFA726)
private val OrangeCardBg = Color(0xFF2A1F0F)

private val BlueAccent = Color(0xFF42A5F5)
private val BlueCardBg = Color(0xFF0F1F2A)

private val GreenAccent = Color(0xFF66BB6A)
private val GreenCardBg = Color(0xFF0F2A1F)

@Composable
fun AlertsHistoryScreen(viewModel: AlertsHistoryViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { AlertsHeader(alertsCount = uiState.alerts.size, onClear = viewModel::clearHistory) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
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
                            val visible = remember { MutableTransitionState(false).apply { targetState = true } }

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

// ═══════════════════════════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AlertsHeader(alertsCount: Int, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clear history button
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Clear history",
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title + Badge + Bell
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alerts",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                if (alertsCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFE53935), CircleShape),
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
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Subtitle count
        Text(
            text = "$alertsCount alerts",
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 6.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// ALERT CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AlertCard(alert: AlertEvent, modifier: Modifier = Modifier) {
    val theme = alertTheme(alert.type)

    // Subtle pulse for critical alerts
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (alert.type == AlertType.SOS_ACTIVATED || alert.type == AlertType.FALL_DETECTED) 0.45f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            theme.accentColor.copy(alpha = pulseAlpha)
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
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
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
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    // If your AlertEvent model has lat/lng, uncomment:
                    // alert.latitude?.let { lat ->
                    //     alert.longitude?.let { lng ->
                    //         Spacer(modifier = Modifier.height(2.dp))
                    //         Text(
                    //             text = String.format(Locale.US, "%.5f, %.5f", lat, lng),
                    //             color = TextMuted,
                    //             fontSize = 12.sp
                    //         )
                    //     }
                    // }
                }
            }

            // Right: Icon in circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(theme.accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = theme.icon,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(24.dp)
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
            tint = TextMuted.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No alerts yet",
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// THEME MAPPING
// ═══════════════════════════════════════════════════════════════

private data class AlertTheme(
    val icon: ImageVector,
    val accentColor: Color,
    val cardBackground: Color
)

private fun alertTheme(type: AlertType): AlertTheme = when (type) {
    AlertType.SOS_ACTIVATED -> AlertTheme(
        Icons.Filled.NotificationsActive, OrangeAccent, OrangeCardBg
    )
    AlertType.FALL_DETECTED -> AlertTheme(
        Icons.Filled.Warning, OrangeAccent, OrangeCardBg
    )
    AlertType.WRISTBAND_REMOVED -> AlertTheme(
        Icons.Filled.PersonOff, OrangeAccent, OrangeCardBg
    )
    AlertType.GEOFENCE_EXITED -> AlertTheme(
        Icons.Filled.LocationOff, OrangeAccent, OrangeCardBg
    )
    AlertType.GEOFENCE_ENTERED -> AlertTheme(
        Icons.Filled.LocationOn, GreenAccent, GreenCardBg
    )
    AlertType.CONNECTION_LOST -> AlertTheme(
        Icons.Filled.CloudOff, BlueAccent, BlueCardBg
    )
    AlertType.CONNECTION_RESTORED -> AlertTheme(
        Icons.AutoMirrored.Filled.DirectionsWalk, BlueAccent, BlueCardBg
    )
}