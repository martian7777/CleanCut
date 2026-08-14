package com.cleancut.bgremover.feature.bgremoval.domain

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri

/** Contract for turning a composited cutout into a saved/shareable artifact. */
interface ExportRepository {
    suspend fun saveToGallery(
        bitmap: Bitmap,
        displayName: String = "CleanCut_${System.currentTimeMillis()}",
    ): Result<Uri>

    fun shareIntent(savedUri: Uri): Intent
}
