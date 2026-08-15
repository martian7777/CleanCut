package com.cleancut.bgremover.feature.magicbrush.domain

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * A user-painted erase mask in full-resolution source-pixel coordinates. [maskBitmap] is
 * ALPHA_8 and sized exactly to [boundingBox] (not the full source image) to stay cheap.
 *
 * Polarity is 255 = erase this pixel, 0 = keep it - the intuitive convention. The bundled
 * MI-GAN model's on-graph convention is inverted (255 = keep, 0 = erase); that inversion is
 * a data-layer detail handled entirely inside [MiGanInpaintingEngine] and must never leak
 * into domain, presentation, or UI code.
 */
data class MaskRegion(
    val maskBitmap: Bitmap,
    val boundingBox: Rect,
)
