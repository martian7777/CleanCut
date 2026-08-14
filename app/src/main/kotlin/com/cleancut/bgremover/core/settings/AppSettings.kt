package com.cleancut.bgremover.core.settings

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    PNG("PNG (Transparent)", "png", "image/png"),
    JPEG("JPEG (White Matte)", "jpg", "image/jpeg"),
    WEBP("WEBP (Ultra Compact)", "webp", "image/webp")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val exportFormat: ExportFormat = ExportFormat.PNG,
    val exportQuality: Int = 100, // 80 - 100
    val highQualityUpsampling: Boolean = true,
)
