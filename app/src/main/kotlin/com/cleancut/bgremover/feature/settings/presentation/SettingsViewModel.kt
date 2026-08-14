package com.cleancut.bgremover.feature.settings.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cleancut.bgremover.core.settings.AppSettings
import com.cleancut.bgremover.core.settings.AppThemeMode
import com.cleancut.bgremover.core.settings.ExportFormat
import com.cleancut.bgremover.core.settings.SettingsManager
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    application: Application,
    private val settingsManager: SettingsManager = SettingsManager.getInstance(application),
) : AndroidViewModel(application) {

    val settings: StateFlow<AppSettings> = settingsManager.settings

    fun onThemeSelected(mode: AppThemeMode) {
        settingsManager.updateThemeMode(mode)
    }

    fun onExportFormatSelected(format: ExportFormat) {
        settingsManager.updateExportFormat(format)
    }

    fun onExportQualityChanged(quality: Int) {
        settingsManager.updateExportQuality(quality)
    }

    fun onHighQualityToggled(enabled: Boolean) {
        settingsManager.updateHighQualityUpsampling(enabled)
    }
}
