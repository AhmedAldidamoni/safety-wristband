package com.safewristband.tracker.presentation.geofence

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safewristband.tracker.R
import com.safewristband.tracker.domain.usecase.GeofenceStatusResult
import com.safewristband.tracker.presentation.theme.AlertRed
import com.safewristband.tracker.presentation.theme.OkGreen
import com.safewristband.tracker.util.Constants
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapView
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions
import org.maplibre.android.plugins.annotation.FillManager
import org.maplibre.android.plugins.annotation.FillOptions
import org.maplibre.android.plugins.annotation.LineManager
import org.maplibre.android.plugins.annotation.LineOptions
import java.util.Locale
import org.maplibre.android.geometry.LatLng as MapLibreLatLng

// ─── Cairo, Egypt ───
private val CAIRO_EGYPT = MapLibreLatLng(30.0444, 31.2357)
// Region zoom: shows Egypt and surrounding countries
private const val REGION_ZOOM = 5.5
// City zoom: when user taps to set a geofence center
private const val CITY_ZOOM = 14.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(viewModel: GeofenceViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Default to Cairo, Egypt if no data and no pending center
    val defaultCenter = if (uiState.pendingCenterLat != null && uiState.pendingCenterLon != null) {
        MapLibreLatLng(uiState.pendingCenterLat!!, uiState.pendingCenterLon!!)
    } else if (uiState.wristbandData != null) {
        MapLibreLatLng(uiState.wristbandData!!.latitude, uiState.wristbandData!!.longitude)
    } else {
        CAIRO_EGYPT
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        //GeofenceHeader()

        Box(modifier = Modifier.fillMaxSize()) {

            // ─── OpenFreeMap — free, full detail, no API key ───
            GeofenceMapView(
                defaultCenter = defaultCenter,
                centerLat = uiState.pendingCenterLat,
                centerLon = uiState.pendingCenterLon,
                radiusMeters = uiState.pendingRadiusMeters,
                wristbandLat = uiState.wristbandData?.latitude,
                wristbandLon = uiState.wristbandData?.longitude,
                onMapTapped = { lat, lon -> viewModel.onMapTapped(lat, lon) },
                modifier = Modifier.fillMaxWidth()
            )

            // ─── Controls Card ───
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Radius header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(18.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.geofence_radius),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.0f %s",
                                uiState.pendingRadiusMeters,
                                stringResource(R.string.unit_meters)
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    val sliderColors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                    Slider(
                        value = uiState.pendingRadiusMeters.toFloat(),
                        onValueChange = { viewModel.onRadiusChanged(it.toDouble()) },
                        valueRange = Constants.MIN_GEOFENCE_RADIUS_METERS.toFloat()..Constants.MAX_GEOFENCE_RADIUS_METERS.toFloat(),
                        colors = sliderColors,
                        interactionSource = interactionSource,
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = interactionSource,
                                colors = sliderColors,
                                thumbSize = DpSize(8.dp, 24.dp)
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                colors = sliderColors,
                                sliderState = sliderState,
                                modifier = Modifier.height(8.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    GeofenceStatusBanner(statusResult = uiState.statusResult)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = viewModel::saveGeofence,
                        enabled = uiState.pendingCenterLat != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.saveSuccess) Icons.Filled.Check else Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.saveSuccess)
                                stringResource(R.string.geofence_saved)
                            else
                                stringResource(R.string.geofence_save),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// GEOFENCE MAP VIEW (OpenFreeMap)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GeofenceMapView(
    defaultCenter: MapLibreLatLng,
    centerLat: Double?,
    centerLon: Double?,
    radiusMeters: Double,
    wristbandLat: Double?,
    wristbandLon: Double?,
    onMapTapped: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapLibreMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
    var fillManager by remember { mutableStateOf<FillManager?>(null) }
    var lineManager by remember { mutableStateOf<LineManager?>(null) }
    var circleManager by remember { mutableStateOf<CircleManager?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val viewCenterLat = centerLat ?: defaultCenter.latitude
    val viewCenterLon = centerLon ?: defaultCenter.longitude

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this
                    onCreate(null)
                    getMapAsync { map ->
                        mapLibreMap = map
                        // ═══════════════════════════════════════════════════════
                        // OPENFREEMAP: Free, full-detail vector tiles. No signup.
                        // ═══════════════════════════════════════════════════════
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
                            val hasCenter = centerLat != null && centerLon != null
                            map.cameraPosition = org.maplibre.android.camera.CameraPosition.Builder()
                                .target(defaultCenter)
                                .zoom(if (hasCenter) CITY_ZOOM else REGION_ZOOM)
                                .build()

                            fillManager = FillManager(this, map, style)
                            lineManager = LineManager(this, map, style)
                            circleManager = CircleManager(this, map, style)

                            map.addOnMapClickListener { latLng ->
                                onMapTapped(latLng.latitude, latLng.longitude)
                                true
                            }
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

        // Camera update when center changes (zoom to city level)
        LaunchedEffect(centerLat, centerLon, mapLibreMap) {
            val lat = centerLat ?: return@LaunchedEffect
            val lon = centerLon ?: return@LaunchedEffect
            mapLibreMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(MapLibreLatLng(lat, lon), CITY_ZOOM)
            )
        }

        // Draw annotations
        LaunchedEffect(
            fillManager, lineManager, circleManager,
            centerLat, centerLon, radiusMeters, wristbandLat, wristbandLon
        ) {
            fillManager?.deleteAll()
            lineManager?.deleteAll()
            circleManager?.deleteAll()

            val center = centerLat ?: viewCenterLat
            val centerLng = centerLon ?: viewCenterLon

            // Geofence circle
            val circlePoints = createCirclePolygon(
                MapLibreLatLng(center, centerLng),
                radiusMeters
            )

            fillManager?.create(
                FillOptions()
                    .withLatLngs(listOf(circlePoints))
                    .withFillColor("#0F766E")
                    .withFillOpacity(0.2f)
            )

            lineManager?.create(
                LineOptions()
                    .withLatLngs(circlePoints)
                    .withLineColor("#0F766E")
                    .withLineWidth(3f)
            )

            // Center dot
            circleManager?.create(
                CircleOptions()
                    .withLatLng(MapLibreLatLng(center, centerLng))
                    .withCircleRadius(8f)
                    .withCircleColor("#0F766E")
                    .withCircleStrokeColor("#FFFFFF")
                    .withCircleStrokeWidth(2f)
            )

            // Wristband marker
            if (wristbandLat != null && wristbandLon != null) {
                circleManager?.create(
                    CircleOptions()
                        .withLatLng(MapLibreLatLng(wristbandLat, wristbandLon))
                        .withCircleRadius(8f)
                        .withCircleColor("#DC2626")
                        .withCircleStrokeColor("#FFFFFF")
                        .withCircleStrokeWidth(2f)
                )
            }
        }

        // Coordinate overlay
        if (centerLat != null && centerLon != null) {
            Text(
                text = String.format(Locale.US, "%.5f, %.5f", centerLat, centerLon),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Tap instruction
        if (centerLat == null) {
            Text(
                text = stringResource(R.string.geofence_tap_to_set),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════════════════════════════

private fun createCirclePolygon(
    center: MapLibreLatLng,
    radiusMeters: Double,
    numPoints: Int = 64
): List<MapLibreLatLng> {
    val points = mutableListOf<MapLibreLatLng>()
    for (i in 0..numPoints) {
        val angle = 2.0 * Math.PI * i / numPoints
        val dx = radiusMeters * kotlin.math.cos(angle)
        val dy = radiusMeters * kotlin.math.sin(angle)
        val dLat = dy / 111000.0
        val dLon = dx / (111000.0 * kotlin.math.cos(Math.toRadians(center.latitude)))
        points.add(
            MapLibreLatLng(
                center.latitude + dLat,
                center.longitude + dLon
            )
        )
    }
    return points
}

@Composable
private fun GeofenceStatusBanner(statusResult: GeofenceStatusResult) {
    val (label, color, icon) = when (statusResult) {
        is GeofenceStatusResult.NotConfigured -> Triple(
            stringResource(R.string.geofence_status_not_set),
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Filled.Info
        )
        is GeofenceStatusResult.Inside -> Triple(
            stringResource(R.string.geofence_status_inside, statusResult.distanceMeters),
            OkGreen,
            Icons.Filled.CheckCircle
        )
        is GeofenceStatusResult.Outside -> Triple(
            stringResource(R.string.geofence_status_outside, statusResult.distanceMeters),
            AlertRed,
            Icons.Filled.Warning
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}