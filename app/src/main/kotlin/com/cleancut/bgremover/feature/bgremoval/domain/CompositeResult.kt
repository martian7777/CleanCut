package com.cleancut.bgremover.feature.bgremoval.domain

import android.graphics.Bitmap

/**
 * Final output of a background-removal run: the source bitmap, mutated in place
 * so its alpha channel now carries the subject cutout. Dimensions always match
 * the original source image exactly - nothing on the kept-pixel path is resampled.
 */
data class CompositeResult(
    val outputBitmap: Bitmap,
    val widthPx: Int,
    val heightPx: Int,
)
