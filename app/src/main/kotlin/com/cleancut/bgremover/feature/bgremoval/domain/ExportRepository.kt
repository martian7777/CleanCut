package com.cleancut.bgremover.feature.bgremoval.domain

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.cleancut.bgremover.core.settings.ExportFormat

/** Contract for turning a composited cutout into a saved/shareable artifact. */
interface ExportRepository {
    suspend fun saveToGallery(
        bitmap: Bitmap,
        displayName: String = "CleanCut_${System.currentTimeMillis()}",
        format: ExportFormat = ExportFormat.PNG,
        quality: Int = 100,
    ): Result<Uri>

    fun shareIntent(savedUri: Uri, mimeType: String = "image/png"): Intent
}
