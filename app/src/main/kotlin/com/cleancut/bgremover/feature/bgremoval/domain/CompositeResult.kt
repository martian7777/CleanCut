package com.cleancut.bgremover.feature.bgremoval.domain

import android.graphics.Bitmap

/**
 * Final output of a background-removal run: the subject cutout bitmap,
 * an optional original preview bitmap for Before/After comparison, and dimensions.
 */
data class CompositeResult(
    val outputBitmap: Bitmap,
    val widthPx: Int,
    val heightPx: Int,
    val originalPreview: Bitmap? = null,
)
