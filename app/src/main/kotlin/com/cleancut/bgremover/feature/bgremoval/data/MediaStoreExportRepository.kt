package com.cleancut.bgremover.feature.bgremoval.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.cleancut.bgremover.core.settings.ExportFormat
import com.cleancut.bgremover.feature.bgremoval.domain.ExportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaStoreExportRepository(private val context: Context) : ExportRepository {

    override suspend fun saveToGallery(
        bitmap: Bitmap,
        displayName: String,
        format: ExportFormat,
        quality: Int,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val fileName = "$displayName.${format.extension}"
            val mimeType = format.mimeType

            val compressFormat = when (format) {
                ExportFormat.PNG -> Bitmap.CompressFormat.PNG
                ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
                ExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/CleanCut")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val insertedUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(IllegalStateException("MediaStore insert failed"))

                resolver.openOutputStream(insertedUri)?.use { stream ->
                    bitmap.compress(compressFormat, quality.coerceIn(1, 100), stream)
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
                    bitmap.compress(compressFormat, quality.coerceIn(1, 100), stream)
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
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

    override fun shareIntent(savedUri: Uri, mimeType: String): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, savedUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, null)
    }
}
