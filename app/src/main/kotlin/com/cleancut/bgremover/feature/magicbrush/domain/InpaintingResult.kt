package com.cleancut.bgremover.feature.magicbrush.domain

import android.graphics.Bitmap

/**
 * [outputBitmap] is the same object reference passed into [InpaintingEngine.inpaint] as
 * `source`, mutated in place - never a second full-size allocation.
 */
data class InpaintingResult(
    val outputBitmap: Bitmap,
    val patch: ErasePatch,
)
