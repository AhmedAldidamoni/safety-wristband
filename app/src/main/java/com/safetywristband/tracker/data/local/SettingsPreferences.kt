package com.safetywristband.tracker.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.safetywristband.tracker.domain.model.AppSettings
import com.safetywristband.tracker.domain.model.ThemeMode
import com.safetywristband.tracker.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object SettingsKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val WRISTBAND_ID = stringPreferencesKey("selected_wristband_id")
}

@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.entries.find { it.name == prefs[SettingsKeys.THEME_MODE] }
                ?: ThemeMode.SYSTEM,
            selectedWristbandId = prefs[SettingsKeys.WRISTBAND_ID] ?: Constants.DEFAULT_WRISTBAND_ID
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[SettingsKeys.THEME_MODE] = mode.name }
    }

    suspend fun setSelectedWristbandId(wristbandId: String) {
        dataStore.edit { prefs -> prefs[SettingsKeys.WRISTBAND_ID] = wristbandId }
    }
}
