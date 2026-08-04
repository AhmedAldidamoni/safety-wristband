package com.safetywristband.tracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.ui.graphics.vector.ImageVector
import com.safetywristband.tracker.R

sealed class Screen(
    val route: String,
    val labelResId: Int,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        "dashboard",
        R.string.nav_dashboard,
        Icons.Filled.Dashboard
    )
    data object LiveMap : Screen(
        "live_map",
        R.string.nav_live_map,
        Icons.Filled.Map
    )
    data object Geofence : Screen(
        "geofence",
        R.string.nav_geofence,
        Icons.Filled.ShareLocation
    )
    data object Alerts : Screen(
        "alerts",
        R.string.nav_alerts,
        Icons.Filled.History
    )
    data object Settings : Screen(
        "settings",
        R.string.nav_settings,
        Icons.Filled.Settings
    )

    companion object {
        val bottomNavItems = listOf(Dashboard, LiveMap, Geofence, Alerts, Settings)
    }
}