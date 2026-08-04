package com.safetywristband.tracker.util

object Constants {
    const val FIREBASE_WRISTBANDS_NODE = "wristbands"
    const val DEFAULT_WRISTBAND_ID = "wristband_01"

    const val STALE_CONNECTION_THRESHOLD_MS = 30_000L

    const val MIN_GEOFENCE_RADIUS_METERS = 20.0
    const val MAX_GEOFENCE_RADIUS_METERS = 2000.0

    const val DATASTORE_SETTINGS = "settings_preferences"
    const val DATASTORE_GEOFENCE = "geofence_preferences"

    const val DATABASE_NAME = "safetywristband.db"
}
