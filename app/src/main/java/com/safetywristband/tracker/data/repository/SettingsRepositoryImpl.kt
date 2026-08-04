package com.safetywristband.tracker.data.repository

import com.safetywristband.tracker.data.local.SettingsPreferences
import com.safetywristband.tracker.domain.model.AppSettings
import com.safetywristband.tracker.domain.model.ThemeMode
import com.safetywristband.tracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = settingsPreferences.settings

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsPreferences.setThemeMode(mode)
    }

    override suspend fun setSelectedWristbandId(wristbandId: String) {
        settingsPreferences.setSelectedWristbandId(wristbandId)
    }
}
