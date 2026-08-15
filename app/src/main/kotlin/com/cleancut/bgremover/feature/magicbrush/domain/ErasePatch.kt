package com.cleancut.bgremover.feature.magicbrush.domain

import android.graphics.Rect

/**
 * One undo/redo unit: the pixels touched by a single erase step, captured only over
 * [bounds] (the context-padded crop actually sent to the model) rather than the whole
 * photo. A 48MP full-bitmap undo history would cost ~192MB/step; this costs roughly
 * bounds.width() * bounds.height() * 4 bytes * 2 - typically a few MB.
 *
 * [beforePixels]/[afterPixels] are row-major ARGB_8888 int arrays sized
 * bounds.width() * bounds.height(), suitable for direct use with
 * [android.graphics.Bitmap.getPixels]/[android.graphics.Bitmap.setPixels].
 */
@Suppress("ArrayInDataClass")
data class ErasePatch(
    val bounds: Rect,
    val beforePixels: IntArray,
    val afterPixels: IntArray,
)
