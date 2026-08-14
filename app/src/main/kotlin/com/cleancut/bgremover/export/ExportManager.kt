package com.cleancut.bgremover.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Saves the composited cutout as a lossless PNG (the only format that keeps the
 * alpha channel) to the gallery, then reuses that same saved Uri for the share
 * sheet - one write path serves both "save" and "share".
 */
class ExportManager(private val context: Context) {

    suspend fun saveToGallery(
        bitmap: Bitmap,
        displayName: String = "CleanCut_${System.currentTimeMillis()}",
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val fileName = "$displayName.png"

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/CleanCut")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val insertedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(IllegalStateException("MediaStore insert failed"))

                resolver.openOutputStream(insertedUri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                } ?: return@withContext Result.failure(IllegalStateException("Unable to open output stream for $insertedUri"))

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(insertedUri, values, null, null)
                insertedUri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val cleanCutDir = File(picturesDir, "CleanCut").apply { mkdirs() }
                val outFile = File(cleanCutDir, fileName)
                FileOutputStream(outFile).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    @Suppress("DEPRECATION")
                    put(MediaStore.Images.Media.DATA, outFile.absolutePath)
                }
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(IllegalStateException("MediaStore insert failed"))
            }

            Result.success(uri)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    fun shareIntent(savedUri: Uri): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, savedUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, null)
    }
}
