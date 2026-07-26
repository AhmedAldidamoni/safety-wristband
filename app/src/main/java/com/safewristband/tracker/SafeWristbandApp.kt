package com.safewristband.tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

@HiltAndroidApp
class SafeWristbandApp : Application() {

    override fun onCreate() {
        super.onCreate()

        MapLibre.getInstance(this)
    }
}