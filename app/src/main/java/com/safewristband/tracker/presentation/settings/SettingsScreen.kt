package com.safewristband.tracker.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safewristband.tracker.domain.model.ConnectionStatus
import com.safewristband.tracker.domain.model.ThemeMode
import com.safewristband.tracker.presentation.theme.SafeWristbandTheme

// ─── Dark Theme Palette (same as Dashboard & Alerts) ───
private val DarkBackground = Color(0xFF0B1120)
private val CardBackground = Color(0xFF151B2B)
private val TealAccent = Color(0xFF00D9C0)
private val TealGlow = TealAccent.copy(alpha = 0.15f)
private val TextPrimary = Color(0xFFE2E8F0)
private val TextSecondary = Color(0xFF8B95A5)
private val TextMuted = Color(0xFF5A6578)
private val AlertRed = Color(0xFFEF5350)
private val AlertRedBg = Color(0xFF2A1515)
private val OkGreen = Color(0xFF66BB6A)
private val WarningAmber = Color(0xFFFFA726)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Custom Header ───
            SettingsHeader()

            AnimatedVisibility(
                visibleState = listState,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 5 }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ─── Connection Status Card ───
                    ConnectionStatusCard(
                        status = uiState.connectionStatus,
                        wristbandId = uiState.settings.selectedWristbandId
                    )

                    // ─── Theme Selector ───
                    ThemeSelectorCard(
                        currentMode = uiState.settings.themeMode,
                        onModeSelected = viewModel::setThemeMode
                    )

                    // ─── Application Info ───
                    AppInfoCard()

                    // ─── Danger Zone: Clear History ───
                    DangerActionCard(
                        isCleared = uiState.historyCleared,
                        onClear = viewModel::clearAlertHistory
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ─── Footer ───
                    Text(
                        text = "SafeWristband v1.0.0",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Smart safety monitoring companion",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SettingsHeader() {
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
                .background(TealAccent.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                tint = TealAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure your wristband",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// CONNECTION STATUS CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ConnectionStatusCard(status: ConnectionStatus, wristbandId: String) {
    val (statusText, statusColor, statusIcon) = when (status) {
        ConnectionStatus.CONNECTED -> Triple("Connected", OkGreen, Icons.Filled.CloudDone)
        ConnectionStatus.STALE -> Triple("Stale Data", WarningAmber, Icons.Filled.CloudQueue)
        ConnectionStatus.DISCONNECTED -> Triple("Disconnected", AlertRed, Icons.Filled.CloudOff)
        ConnectionStatus.CONNECTING -> Triple("Connecting...", WarningAmber, Icons.Filled.Sync)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (status != ConnectionStatus.CONNECTED) 0.6f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    SettingsCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Animated status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = statusColor.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                        .border(2.dp, statusColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Firebase",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Wristband ID: $wristbandId",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// THEME SELECTOR CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ThemeSelectorCard(currentMode: ThemeMode, onModeSelected: (ThemeMode) -> Unit) {
    SettingsCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Appearance",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = currentMode == mode
                    val (icon, label) = when (mode) {
                        ThemeMode.LIGHT -> Icons.Filled.WbSunny to "Light"
                        ThemeMode.DARK -> Icons.Filled.NightsStay to "Dark"
                        ThemeMode.SYSTEM -> Icons.Filled.SettingsSuggest to "Auto"
                    }

                    val bgColor = if (isSelected) TealGlow else Color.Transparent
                    val borderColor = if (isSelected) TealAccent else TextMuted.copy(alpha = 0.3f)
                    val iconTint = if (isSelected) TealAccent else TextMuted
                    val textColor = if (isSelected) TextPrimary else TextMuted

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// APP INFO CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AppInfoCard() {
    SettingsCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(TealAccent.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Watch,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SafeWristband",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Smart safety monitoring companion app.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DANGER ACTION CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DangerActionCard(isCleared: Boolean, onClear: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isCleared) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    SettingsCard(
        backgroundColor = if (isCleared) CardBackground else AlertRedBg
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isCleared) TextMuted else AlertRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Danger Zone",
                    color = if (isCleared) TextMuted else AlertRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isCleared)
                    "All local alert history has been removed."
                else
                    "Permanently remove all locally stored alert events. This action cannot be undone.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onClear,
                enabled = !isCleared,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isCleared) TextMuted else AlertRed
                ),
                border = ButtonDefaults.outlinedButtonBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isCleared) TextMuted.copy(alpha = 0.3f) else AlertRed.copy(alpha = 0.5f))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCleared) "History Cleared" else "Clear Local Alert History",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// REUSABLE CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SettingsCard(
    backgroundColor: Color = CardBackground,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════

@Preview(showBackground = true, backgroundColor = 0xFF0B1120)
@Composable
private fun SettingsScreenPreview() {
    // Mock preview data
    SafeWristbandTheme(darkTheme = true) {
        Box(modifier = Modifier.background(DarkBackground)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingsHeader()
                ConnectionStatusCard(
                    status = ConnectionStatus.CONNECTED,
                    wristbandId = "WB-7842-Alpha"
                )
                ThemeSelectorCard(
                    currentMode = ThemeMode.DARK,
                    onModeSelected = {}
                )
                AppInfoCard()
                DangerActionCard(isCleared = false, onClear = {})
            }
        }
    }
}