package com.cleancut.bgremover.core.common

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

data class ImageBounds(val width: Int, val height: Int)

object ImageDecodeUtils {

    /**
     * Copies [uri] into a local cache file and returns it. Some sources - notably
     * the Android Photo Picker's content://media/picker/... Uris, especially on
     * MIUI - aren't reliably reopenable, so callers should read this file's bytes
     * once via [readSourceBytes] rather than reopening the file per decode pass.
     */
    fun copyToLocalCache(cacheDir: File, resolver: ContentResolver, uri: Uri): File {
        val cacheFile = File(cacheDir, "cleancut_source_${System.currentTimeMillis()}")
        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
        } ?: error("Unable to open $uri")
        return cacheFile
    }

    /**
     * Reads [file] into memory once. Every decode pass below (bounds check,
     * full-res decode, downscaled decode, EXIF read) works off this buffer instead
     * of reopening the cache file - on some devices the file has been observed to
     * become unreadable (e.g. low-storage cache eviction) between an early decode
     * pass and a later one over the same still-slow multi-step pipeline.
     */
    fun readSourceBytes(file: File): ByteArray = file.readBytes()

    fun readBounds(bytes: ByteArray): ImageBounds {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return ImageBounds(options.outWidth, options.outHeight)
    }

    private fun readOrientation(bytes: ByteArray): Int {
        return ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }

    /**
     * Decodes the source at full resolution (inSampleSize = 1), mutable, with EXIF
     * rotation applied. This is the one full-size buffer that becomes the final
     * composited output in place - the kept-pixel path never resamples.
     */
    fun decodeFullResolutionMutable(bytes: ByteArray): Bitmap {
        val options = BitmapFactory.Options().apply {
            inSampleSize = 1
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inMutable = true
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: error("Unable to decode source image")

        return applyExifOrientation(decoded, readOrientation(bytes))
    }

    /**
     * Decodes a small downscaled copy purely for segmentation input - the model
     * doesn't need (or want) a 48MP input. This bitmap should be recycled by the
     * caller as soon as the mask has been read out of it.
     */
    fun decodeDownscaledForSegmentation(bytes: ByteArray, targetLongEdgePx: Int): Bitmap {
        val bounds = readBounds(bytes)
        val longEdge = maxOf(bounds.width, bounds.height)
        val sampleSize = calculateInSampleSize(longEdge, targetLongEdgePx)

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: error("Unable to decode source image")

        return applyExifOrientation(decoded, readOrientation(bytes))
    }

    private fun calculateInSampleSize(sourceLongEdge: Int, targetLongEdge: Int): Int {
        var sampleSize = 1
        var edge = sourceLongEdge
        while (edge / 2 >= targetLongEdge) {
            edge /= 2
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return source
        }

        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotated !== source) {
            source.recycle()
        }
        return rotated
    }
}
