package com.safetywristband.tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

@HiltAndroidApp
class SafetyWristbandApp : Application() {
    override fun onCreate() {
        super.onCreate()

        MapLibre.getInstance(this)
    }
}