package com.safewristband.tracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    data object LiveMap : Screen("live_map", "Live Map", Icons.Filled.Map)
    data object Geofence : Screen("geofence", "Geofence", Icons.Filled.Shield)
    data object Alerts : Screen("alerts", "Alerts", Icons.Filled.History)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(Dashboard, LiveMap, Geofence, Alerts, Settings)
    }
}
