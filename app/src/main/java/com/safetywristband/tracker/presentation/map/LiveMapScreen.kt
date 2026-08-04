package com.safetywristband.tracker.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safetywristband.tracker.R
import com.safetywristband.tracker.presentation.components.ErrorView
import com.safetywristband.tracker.presentation.components.LoadingIndicator
import com.safetywristband.tracker.presentation.theme.OkGreen
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import java.util.Locale
import org.maplibre.android.geometry.LatLng as MapLibreLatLng

// ─── Cairo, Egypt ───
private val CAIRO_EGYPT = MapLibreLatLng(30.0444, 31.2357)
// Region zoom: shows Egypt + surrounding countries (Middle East / North Africa)
private const val REGION_ZOOM = 5.5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMapScreen(viewModel: LiveMapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        //LiveMapHeader()

        when {
            uiState.isLoading && uiState.wristbandData == null ->
                LoadingIndicator(modifier = Modifier.padding(16.dp))

            uiState.errorMessage != null && uiState.wristbandData == null ->
                ErrorView(
                    message = uiState.errorMessage ?: stringResource(R.string.map_error_location),
                    modifier = Modifier.padding(16.dp)
                )

            else -> LiveMapContent(
                uiState = uiState,
                onToggleAutoFollow = viewModel::toggleAutoFollow
            )
        }
    }
}

@Composable
private fun LiveMapContent(
    uiState: LiveMapUiState,
    onToggleAutoFollow: () -> Unit
) {
    val data = uiState.wristbandData
    val hasData = data != null
    // If we have wristband data, use it; otherwise default to Cairo, Egypt
    val position = if (hasData) {
        MapLibreLatLng(data.latitude, data.longitude)
    } else {
        CAIRO_EGYPT
    }

    var mapLibreMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var circleManager by remember { mutableStateOf<CircleManager?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val cardState = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(modifier = Modifier.fillMaxSize()) {
        // ─── OpenFreeMap — completely free, full detail, no API key ───
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this
                    onCreate(null)
                    getMapAsync { map ->
                        mapLibreMap = map
                        // ═══════════════════════════════════════════════════════
                        // OPENFREEMAP: Free, full-detail vector tiles. No signup.
                        // Shows streets, cities, buildings, labels — like Google Maps.
                        // ═══════════════════════════════════════════════════════
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                .target(position)
                                .zoom(if (hasData) 16.0 else REGION_ZOOM)
                                .build()

                            circleManager = CircleManager(this, map, style)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        DisposableEffect(Unit) {
            onDispose {
                mapViewRef?.onDestroy()
            }
        }

        // Auto-follow camera when data updates
        LaunchedEffect(position, uiState.autoFollow, mapLibreMap, hasData) {
            if (uiState.autoFollow && mapLibreMap != null && hasData) {
                mapLibreMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, 16.0)
                )
            }
        }

        // Update wristband marker
        LaunchedEffect(position, circleManager, hasData) {
            circleManager?.deleteAll()
            if (hasData) {
                circleManager?.create(
                    CircleOptions()
                        .withLatLng(position)
                        .withCircleRadius(10f)
                        .withCircleColor("#DC2626")
                        .withCircleStrokeColor("#FFFFFF")
                        .withCircleStrokeWidth(2f)
                )
            }
        }

        // ─── Auto-follow FAB ───
        FloatingActionButton(
            onClick = onToggleAutoFollow,
            containerColor = if (uiState.autoFollow)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface,
            contentColor = if (uiState.autoFollow)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = stringResource(R.string.auto_follow_toggle)
            )
        }

        // ─── COMPACT Bottom Info Card (2x2 grid, NO speed, NO movement) ───
        AnimatedVisibility(
            visibleState = cardState,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (data != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header row with switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val dotColor = if (uiState.autoFollow) OkGreen
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(dotColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.auto_follow),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Switch(
                                checked = uiState.autoFollow,
                                onCheckedChange = { onToggleAutoFollow() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compact 2x2 grid — NO speed, NO movement
                        Row(modifier = Modifier.fillMaxWidth()) {
                            CompactInfoItem(
                                icon = Icons.Filled.MyLocation,
                                label = stringResource(R.string.latitude),
                                value = String.format(Locale.US, "%.5f", data.latitude),
                                modifier = Modifier.weight(1f)
                            )
                            CompactInfoItem(
                                icon = Icons.Filled.MyLocation,
                                label = stringResource(R.string.longitude),
                                value = String.format(Locale.US, "%.5f", data.longitude),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            CompactInfoItem(
                                icon = Icons.Filled.Height,
                                label = stringResource(R.string.altitude),
                                value = String.format(
                                    Locale.US, "%.1f %s", data.altitude,
                                    stringResource(R.string.unit_meters)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            CompactInfoItem(
                                icon = Icons.Filled.Sensors,
                                label = stringResource(R.string.gps_accuracy),
                                value = String.format(
                                    Locale.US, "±%.1f %s", data.accuracy,
                                    stringResource(R.string.unit_meters)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(end = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}