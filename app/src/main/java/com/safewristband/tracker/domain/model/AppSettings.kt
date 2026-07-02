package com.safewristband.tracker.domain.model

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedWristbandId: String = "wristband_01"
)
