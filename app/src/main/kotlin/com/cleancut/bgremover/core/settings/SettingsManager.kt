package com.cleancut.bgremover.core.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeModeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }

        val formatStr = prefs.getString(KEY_EXPORT_FORMAT, ExportFormat.PNG.name) ?: ExportFormat.PNG.name
        val exportFormat = try {
            ExportFormat.valueOf(formatStr)
        } catch (_: Exception) {
            ExportFormat.PNG
        }

        val quality = prefs.getInt(KEY_EXPORT_QUALITY, 100)
        val highQualityUpsampling = prefs.getBoolean(KEY_HIGH_QUALITY, true)

        return AppSettings(
            themeMode = themeMode,
            exportFormat = exportFormat,
            exportQuality = quality,
            highQualityUpsampling = highQualityUpsampling,
        )
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateExportFormat(exportFormat: ExportFormat) {
        prefs.edit().putString(KEY_EXPORT_FORMAT, exportFormat.name).apply()
        _settings.value = _settings.value.copy(exportFormat = exportFormat)
    }

    fun updateExportQuality(quality: Int) {
        prefs.edit().putInt(KEY_EXPORT_QUALITY, quality).apply()
        _settings.value = _settings.value.copy(exportQuality = quality)
    }

    fun updateHighQualityUpsampling(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_QUALITY, enabled).apply()
        _settings.value = _settings.value.copy(highQualityUpsampling = enabled)
    }

    companion object {
        private const val PREFS_NAME = "cleancut_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_EXPORT_FORMAT = "export_format"
        private const val KEY_EXPORT_QUALITY = "export_quality"
        private const val KEY_HIGH_QUALITY = "high_quality"

        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
