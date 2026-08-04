package com.safetywristband.tracker.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safetywristband.tracker.R
import com.safetywristband.tracker.domain.model.ConnectionStatus
import com.safetywristband.tracker.domain.model.MovementStatus
import com.safetywristband.tracker.domain.model.WristbandData
import com.safetywristband.tracker.presentation.components.ErrorView
import com.safetywristband.tracker.presentation.components.LoadingIndicator
import com.safetywristband.tracker.presentation.theme.AlertRed
import com.safetywristband.tracker.presentation.theme.OkGreen
import com.safetywristband.tracker.presentation.theme.SafetyWristbandTheme
import com.safetywristband.tracker.presentation.theme.WarningAmber
import com.safetywristband.tracker.util.DateTimeUtils
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel(), onClickAlerts: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader()

        when {
            uiState.isLoading && uiState.wristbandData == null ->
                LoadingIndicator(modifier = Modifier.padding(16.dp))

            uiState.errorMessage != null && uiState.wristbandData == null ->
                ErrorView(
                    message = uiState.errorMessage ?: stringResource(R.string.something_went_wrong),
                    modifier = Modifier.padding(16.dp)
                )

            else -> DashboardContent(uiState = uiState, onClickAlerts)
        }
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Dashboard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = stringResource(R.string.dashboard_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DashboardContent(uiState: DashboardUiState, onClickAlerts: () -> Unit) {
    val data = uiState.wristbandData
    val listState = remember { MutableTransitionState(false).apply { targetState = true } }

    // ═══════════════════════════════════════════════════════════════
    // LOCAL BATTERY SIMULATION (drains 100% → 0% over 60 seconds)
    // ═══════════════════════════════════════════════════════════════
    val batteryLevel = rememberBatterySimulation()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        AnimatedVisibility(
            visibleState = listState,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(animationSpec = tween(600)) { it / 4 }
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                // ─── Connection Ring ───
                ConnectionStatusRing(
                    status = uiState.connectionStatus,
                    lastUpdate = data?.let { DateTimeUtils.timeAgo(it.timestamp) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Status Pills ───
                StatusPillsRow(
                    connectionStatus = uiState.connectionStatus,
                    movement = data?.movement,
                    fallDetected = data?.fallDetected ?: false,
                    sos = data?.sos ?: false
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ─── Live Data Section ───
                SectionHeader(title = stringResource(R.string.section_live_data))

                Spacer(modifier = Modifier.height(12.dp))

                // ─── Location Card ───
                LocationCard(
                    latitude = data?.latitude,
                    longitude = data?.longitude,
                    accuracy = data?.accuracy?.toFloat()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ═══════════════════════════════════════════════════════════════
                // METRICS GRID — Icon colors only (no tile highlighting)
                // ═══════════════════════════════════════════════════════════════
                val speedValue = data?.speed ?: 0.0
                val accuracyValue = data?.accuracy ?: 999.0

                val metrics = listOf(
                    MetricItem(
                        title = stringResource(R.string.speed),
                        value = data?.let { String.format(Locale.US, "%.1f", it.speed) } ?: "--",
                        unit = stringResource(R.string.unit_meters_per_second),
                        icon = Icons.Filled.Speed,
                        iconColor = getSpeedColor(speedValue)
                    ),
                    MetricItem(
                        title = stringResource(R.string.battery),
                        value = String.format(Locale.US, "%.0f%%", batteryLevel),
                        unit = "",
                        icon = Icons.Filled.BatteryFull,
                        iconColor = getBatteryColor(batteryLevel)
                    ),
                    MetricItem(
                        title = stringResource(R.string.gps_accuracy),
                        value = data?.let { String.format(Locale.US, "±%.1f", it.accuracy) } ?: "--",
                        unit = stringResource(R.string.unit_meters),
                        icon = Icons.Filled.Sensors,
                        iconColor = getAccuracyColor(accuracyValue)
                    ),
                    MetricItem(
                        title = stringResource(R.string.altitude),
                        value = data?.let { String.format(Locale.US, "%.1f", it.altitude) } ?: "--",
                        unit = stringResource(R.string.unit_meters),
                        icon = Icons.Filled.Height,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                )

                SimpleGrid(columns = 2, items = metrics)

                Spacer(modifier = Modifier.height(16.dp))

                // ─── Alert Banners ───
                if (data?.fallDetected == true) {
                    AlertBanner(
                        title = stringResource(R.string.alert_fall_title),
                        message = stringResource(R.string.alert_fall_message),
                        icon = Icons.Filled.Warning,
                        color = AlertRed,
                        isPulsing = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (data?.sos == true) {
                    AlertBanner(
                        title = stringResource(R.string.alert_sos_title),
                        message = stringResource(R.string.alert_sos_message),
                        icon = Icons.Filled.NotificationsActive,
                        color = AlertRed,
                        isPulsing = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (data?.bandRemoved == true) {
                    AlertBanner(
                        title = stringResource(R.string.alert_band_title),
                        message = stringResource(R.string.alert_band_message),
                        icon = Icons.Filled.PersonOff,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.activeAlerts.isNotEmpty()) {
                    ActiveAlertsCard(count = uiState.activeAlerts.size, onClickAlerts)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// COLOR HELPERS — Three-color system for icons only
// ═══════════════════════════════════════════════════════════════

/** Speed: Green (safe ≤2) → Amber (warning >2) → Red (dangerous >5) */
private fun getSpeedColor(speed: Double): Color {
    return when {
        speed > 5.0 -> AlertRed      // Very high — dangerous
        speed > 2.0 -> WarningAmber  // High — warning
        else -> OkGreen              // Normal — safe
    }
}

/** GPS Accuracy: Green (accurate ≤5m) → Amber (fair >5m) → Red (poor >20m) */
private fun getAccuracyColor(accuracy: Double): Color {
    return when {
        accuracy > 20.0 -> AlertRed      // Very inaccurate
        accuracy > 5.0 -> WarningAmber   // Less accurate
        else -> OkGreen                  // Accurate
    }
}

/** Battery: Green (high ≥50%) → Amber (low <50%) → Red (critical <20%) */
private fun getBatteryColor(level: Float): Color {
    return when {
        level < 20f -> AlertRed      // Critical — very low
        level < 50f -> WarningAmber  // Low — warning
        else -> OkGreen              // High — good
    }
}

// ═══════════════════════════════════════════════════════════════
// BATTERY SIMULATION — Local, no actual data needed
// ═══════════════════════════════════════════════════════════════

@Composable
private fun rememberBatterySimulation(): Float {
    // Simulates battery draining from 85% to 0% over 100 mins, then restarts
    val infiniteTransition = rememberInfiniteTransition(label = "battery")
    val batteryLevel by infiniteTransition.animateFloat(
        initialValue = 85f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "batteryDrain"
    )
    return batteryLevel
}

// ─── Components (unchanged except MetricCard) ───

@Composable
private fun ConnectionStatusRing(
    status: ConnectionStatus,
    lastUpdate: String?
) {
    val isConnected = status == ConnectionStatus.CONNECTED
    val ringColor = when (status) {
        ConnectionStatus.CONNECTED -> OkGreen
        ConnectionStatus.STALE -> WarningAmber
        ConnectionStatus.DISCONNECTED -> AlertRed
        ConnectionStatus.CONNECTING -> WarningAmber
    }

    val statusText = when (status) {
        ConnectionStatus.CONNECTED -> stringResource(R.string.status_connected)
        ConnectionStatus.STALE -> stringResource(R.string.status_stale)
        ConnectionStatus.DISCONNECTED -> stringResource(R.string.status_disconnected)
        ConnectionStatus.CONNECTING -> stringResource(R.string.status_connecting)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (!isConnected) 1.3f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = statusText,
            color = ringColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            if (!isConnected) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .background(ringColor.copy(alpha = 0.15f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(ringColor.copy(alpha = 0.08f), CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ringColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    ringColor,
                                    ringColor.copy(alpha = 0.3f),
                                    ringColor
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Watch,
                        contentDescription = null,
                        tint = ringColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        lastUpdate?.let {
            Text(
                text = stringResource(R.string.last_update, it),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun StatusPillsRow(
    connectionStatus: ConnectionStatus,
    movement: MovementStatus?,
    fallDetected: Boolean,
    sos: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        val pills = listOf(
            PillData(
                label = if (connectionStatus == ConnectionStatus.CONNECTED)
                    stringResource(R.string.pill_online) else stringResource(R.string.pill_offline),
                isActive = connectionStatus == ConnectionStatus.CONNECTED,
                activeColor = OkGreen
            ),
            PillData(
                label = if (movement != null && movement != MovementStatus.STATIONARY)
                    stringResource(R.string.pill_moving) else stringResource(R.string.pill_idle),
                isActive = movement != null && movement != MovementStatus.STATIONARY,
                activeColor = WarningAmber
            ),
            PillData(
                label = stringResource(R.string.pill_fall),
                isActive = fallDetected,
                activeColor = AlertRed
            ),
            PillData(
                label = stringResource(R.string.pill_sos),
                isActive = sos,
                activeColor = AlertRed
            )
        )

        pills.forEach { pill ->
            val bgColor = if (pill.isActive) pill.activeColor.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
            val borderColor = if (pill.isActive) pill.activeColor
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            val textColor = if (pill.isActive) pill.activeColor
            else MaterialTheme.colorScheme.onSurfaceVariant

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = bgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = pill.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (pill.isActive) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LocationCard(
    latitude: Double?,
    longitude: Double?,
    accuracy: Float?
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.current_location),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoordinateColumn(
                    label = stringResource(R.string.latitude),
                    value = latitude?.let { String.format(Locale.US, "%.6f", it) } ?: "--"
                )
                CoordinateColumn(
                    label = stringResource(R.string.longitude),
                    value = longitude?.let { String.format(Locale.US, "%.6f", it) } ?: "--"
                )
            }

            accuracy?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.accuracy_meters, it),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CoordinateColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricCard(metric: MetricItem, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface  // Always same surface color
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = null,
                tint = metric.iconColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = metric.value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (metric.unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = metric.unit,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = metric.title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AlertBanner(
    title: String,
    message: String,
    icon: ImageVector,
    color: Color,
    isPulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alertPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertPulse"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isPulsing) Modifier.scale(pulseScale) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ActiveAlertsCard(count: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(WarningAmber.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Badge(
                    containerColor = WarningAmber,
                    contentColor = Color.Black
                ) {
                    Text(text = count.toString(), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.active_alerts),
                color = WarningAmber,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = WarningAmber
            )
        }
    }
}

// ─── Grid Layout ───

@Composable
private fun SimpleGrid(columns: Int, items: List<MetricItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(metric = item, modifier = Modifier.fillMaxWidth())
                    }
                }
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─── Data Classes ───

private data class PillData(
    val label: String,
    val isActive: Boolean,
    val activeColor: Color
)

private data class MetricItem(
    val title: String,
    val value: String,
    val unit: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    val sampleState = DashboardUiState(
        isLoading = false,
        wristbandData = WristbandData(
            wristbandId = "12345",
            latitude = 37.774900,
            longitude = -122.419400,
            altitude = 120.5,
            accuracy = 3.2,
            speed = 7.9,
            movement = MovementStatus.WALKING,
            fallDetected = false,
            bandRemoved = false,
            sos = false,
            timestamp = System.currentTimeMillis()
        ),
        connectionStatus = ConnectionStatus.CONNECTED,
        activeAlerts = emptyList()
    )

    SafetyWristbandTheme {
        DashboardContent(
            uiState = sampleState
        ) {}
    }
}