package com.safetywristband.tracker.domain.repository

import com.safetywristband.tracker.domain.model.AppSettings
import com.safetywristband.tracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setSelectedWristbandId(wristbandId: String)
}
