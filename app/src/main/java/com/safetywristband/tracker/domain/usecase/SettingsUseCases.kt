package com.safetywristband.tracker.domain.usecase

import com.safetywristband.tracker.domain.model.AppSettings
import com.safetywristband.tracker.domain.model.ThemeMode
import com.safetywristband.tracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.observeSettings()
}

class SetThemeModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(mode: ThemeMode) = settingsRepository.setThemeMode(mode)
}
