package com.safetywristband.tracker.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = SafetyTeal,
    onPrimary = Color.White,
    secondary = SafetyTealLight,
    tertiary = WarningAmber,
    error = AlertRed,
    errorContainer = ErrorContainerLight,
    background = SurfaceLight,
    surface = SurfaceContainerLight
)

private val DarkColors = darkColorScheme(
    primary = SafetyTealLight,
    onPrimary = SafetyTealDark,
    secondary = SafetyTeal,
    tertiary = WarningAmber,
    error = AlertRed,
    errorContainer = ErrorContainerDark,
    background = SurfaceDark,
    surface = SurfaceContainerDark
)

@Composable
fun SafetyWristbandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SafetyWristbandTypography,
        content = content
    )
}
