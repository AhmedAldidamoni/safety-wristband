package com.safewristband.tracker.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safewristband.tracker.domain.model.AlertEvent
import com.safewristband.tracker.domain.model.AlertType
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.MovementStatus
import com.safewristband.tracker.domain.model.WristbandData
import com.safewristband.tracker.presentation.components.ErrorView
import com.safewristband.tracker.presentation.components.LoadingIndicator
import com.safewristband.tracker.presentation.theme.AlertRed
import com.safewristband.tracker.presentation.theme.SafeWristbandTheme
import com.safewristband.tracker.util.DateTimeUtils
import java.util.Locale

// ─── Color Palette (add these to your theme or use locally) ───
private val DarkBackground = Color(0xFF0B1120)
private val CardBackground = Color(0xFF151B2B)
private val CardBackgroundElevated = Color(0xFF1E2538)
private val TealAccent = Color(0xFF00D9C0)
private val OrangeAccent = Color(0xFFFFA726)
private val TextPrimary = Color(0xFFE2E8F0)
private val TextSecondary = Color(0xFF8B95A5)
private val TextMuted = Color(0xFF5A6578)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Wristband",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        when {
            uiState.isLoading && uiState.wristbandData == null ->
                LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.errorMessage != null && uiState.wristbandData == null ->
                ErrorView(
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
    val listState = remember { MutableTransitionState(false).apply { targetState = true } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(DarkBackground)
    ) {
        // ─── Animated Entrance for all content ───
        AnimatedVisibility(
            visibleState = listState,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(animationSpec = tween(600)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ─── 1. CONNECTION STATUS RING ───
                ConnectionStatusRing(
                    status = uiState.connectionStatus,
                    lastUpdate = data?.let { DateTimeUtils.timeAgo(it.timestamp) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ─── 2. STATUS PILLS ───
                StatusPillsRow(
                    connectionStatus = uiState.connectionStatus,
                    movement = data?.movement,
                    fallDetected = data?.fallDetected ?: false,
                    sos = data?.sos ?: false
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ─── 3. LIVE DATA SECTION ───
                SectionHeader(title = "Live Data")

                Spacer(modifier = Modifier.height(12.dp))

                // ─── 4. LOCATION CARD (Full Width) ───
                LocationCard(
                    latitude = data?.latitude,
                    longitude = data?.longitude,
                    accuracy = data?.accuracy?.toFloat()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ─── 5. METRICS GRID ───
                val metrics = listOf(
                    MetricItem(
                        title = "Speed",
                        value = data?.let { String.format(Locale.US, "%.1f", it.speed) } ?: "--",
                        unit = "m/s",
                        icon = Icons.Filled.Speed,
                        color = TealAccent,
                        highlight = data?.let { it.speed > 2.0 } ?: false
                    ),
                    MetricItem(
                        title = "Altitude",
                        value = data?.let { String.format(Locale.US, "%.1f", it.altitude) } ?: "--",
                        unit = "m",
                        icon = Icons.Filled.Height,
                        color = TealAccent
                    ),
                    MetricItem(
                        title = "GPS Accuracy",
                        value = data?.let { String.format(Locale.US, "±%.1f", it.accuracy) } ?: "--",
                        unit = "m",
                        icon = Icons.Filled.Sensors,
                        color = if ((data?.accuracy ?: 999.0) < 5.0) TealAccent else OrangeAccent
                    ),
                    MetricItem(
                        title = "Movement",
                        value = data?.movement?.name?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        unit = "",
                        icon = Icons.Filled.SportsScore,
                        color = if (data?.movement == MovementStatus.RUNNING) OrangeAccent else TealAccent,
                        highlight = data?.movement == MovementStatus.RUNNING
                    )
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp),
                    userScrollEnabled = false
                ) {
                    items(metrics, key = { it.title }) { metric ->
                        MetricCard(
                            metric = metric,
                            modifier = Modifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─── 6. ALERT BANNERS ───
                if (data?.fallDetected == true) {
                    AlertBanner(
                        title = "Fall Detected",
                        message = "Emergency protocol activated",
                        icon = Icons.Filled.Warning,
                        color = AlertRed,
                        modifier = Modifier.animateEnterExit(
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (data?.sos == true) {
                    AlertBanner(
                        title = "SOS ACTIVE",
                        message = "Alert sent to emergency contacts",
                        icon = Icons.Filled.NotificationsActive,
                        color = AlertRed,
                        isPulsing = true,
                        modifier = Modifier.animateEnterExit(
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (data?.bandRemoved == true) {
                    AlertBanner(
                        title = "Band Removed",
                        message = "Device is not being worn",
                        icon = Icons.Filled.PersonOff,
                        color = OrangeAccent,
                        modifier = Modifier.animateEnterExit(
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ─── 7. ACTIVE ALERTS COUNT ───
                if (uiState.activeAlerts.isNotEmpty()) {
                    ActiveAlertsCard(count = uiState.activeAlerts.size)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// COMPONENTS
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ConnectionStatusRing(
    status: ConnectionStatus,
    lastUpdate: String?
) {
    val isConnected = status == ConnectionStatus.CONNECTED
    val ringColor = when (status) {
        ConnectionStatus.CONNECTED -> TealAccent
        ConnectionStatus.STALE -> OrangeAccent
        ConnectionStatus.DISCONNECTED -> AlertRed
        ConnectionStatus.CONNECTING -> OrangeAccent
    }

    // Animated pulse for non-connected states
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
        // Status text above ring
        Text(
            text = if (isConnected) "Connected" else status.name.lowercase()
                .replaceFirstChar { it.uppercase() },
            color = ringColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            // Outer glow pulse
            if (!isConnected) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .background(ringColor.copy(alpha = 0.15f), CircleShape)
                )
            }

            // Static outer ring glow
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(ringColor.copy(alpha = 0.08f), CircleShape)
            )

            // Middle ring
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

            // Inner solid ring with border
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(CardBackgroundElevated, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animated border
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
                        .background(CardBackgroundElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Watch,
                        contentDescription = "Wristband",
                        tint = ringColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Last update text
        lastUpdate?.let {
            Text(
                text = "Last update: $it",
                color = TextMuted,
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
                label = when (connectionStatus) {
                    ConnectionStatus.CONNECTED -> "Online"
                    else -> "Offline"
                },
                isActive = connectionStatus == ConnectionStatus.CONNECTED,
                activeColor = TealAccent
            ),
            PillData(
                label = movement?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Idle",
                isActive = movement != null && movement != MovementStatus.STATIONARY,
                activeColor = OrangeAccent
            ),
            PillData(
                label = "Fall",
                isActive = fallDetected,
                activeColor = AlertRed
            ),
            PillData(
                label = "SOS",
                isActive = sos,
                activeColor = AlertRed
            )
        )

        pills.forEach { pill ->
            val bgColor = if (pill.isActive) pill.activeColor.copy(alpha = 0.15f) else CardBackground
            val borderColor = if (pill.isActive) pill.activeColor else TextMuted.copy(alpha = 0.3f)
            val textColor = if (pill.isActive) pill.activeColor else TextSecondary

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
                .background(TealAccent, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TextPrimary,
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
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Current Location",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CoordinateColumn(
                    label = "Latitude",
                    value = latitude?.let { String.format(Locale.US, "%.6f", it) } ?: "--"
                )
                CoordinateColumn(
                    label = "Longitude",
                    value = longitude?.let { String.format(Locale.US, "%.6f", it) } ?: "--"
                )
            }

            accuracy?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Accuracy: ±${String.format(Locale.US, "%.1f", it)} m",
                    color = TextMuted,
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
            color = TextMuted,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = TealAccent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MetricCard(
    metric: MetricItem,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "highlight")
    val highlightAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (metric.highlight) 0.6f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlightAlpha"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (metric.highlight)
                metric.color.copy(alpha = highlightAlpha * 0.3f)
            else
                CardBackground
        ),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = null,
                tint = metric.color,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = metric.value,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (metric.unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = metric.unit,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = metric.title,
                color = TextMuted,
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
    modifier: Modifier = Modifier,
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
        modifier = modifier
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
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ActiveAlertsCard(count: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(OrangeAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Badge(
                    containerColor = OrangeAccent,
                    contentColor = Color.Black
                ) {
                    Text(text = count.toString(), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Active Alerts",
                color = OrangeAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = OrangeAccent
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════════

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
    val color: Color,
    val highlight: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
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

    SafeWristbandTheme(darkTheme = true) {
        DashboardContent(
            uiState = sampleState,
            padding = PaddingValues(0.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
private fun DashboardAlertPreview() {
    val sampleState = DashboardUiState(
        isLoading = false,
        wristbandData = WristbandData(
            wristbandId = "12345",
            latitude = 46.672860,
            longitude = 24.714557,
            altitude = 608.0,
            accuracy = 2.2,
            speed = 7.9,
            movement = MovementStatus.RUNNING,
            fallDetected = false,
            bandRemoved = true,
            sos = true,
            timestamp = System.currentTimeMillis()
        ),
        connectionStatus = ConnectionStatus.CONNECTED,
        activeAlerts = listOf(
            AlertEvent(
                type = AlertType.FALL_DETECTED,
                message = ",ugugu",
                timestamp = 465415
            )
        )
    )

    SafeWristbandTheme(darkTheme = true) {
        DashboardContent(
            uiState = sampleState,
            padding = PaddingValues(0.dp)
        )
    }
}