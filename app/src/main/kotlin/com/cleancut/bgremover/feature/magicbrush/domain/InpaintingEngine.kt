package com.cleancut.bgremover.feature.magicbrush.domain

import android.graphics.Bitmap
import java.io.Closeable

enum class InpaintingStage {
    LOADING_MODEL,
    PREPARING_MASK,
    INPAINTING,
    DONE,
}

sealed class InpaintingError : Exception() {
    /** Erase was requested with nothing painted. */
    object EmptyMask : InpaintingError()

    /** Inference ran out of memory on this device for this image size. */
    object OutOfMemory : InpaintingError()

    /** The bundled ONNX model asset failed to load into an inference session. */
    data class ModelLoadFailed(override val cause: Throwable) : InpaintingError()

    data class InferenceFailed(override val cause: Throwable) : InpaintingError()
}

interface InpaintingEngine : Closeable {
    /**
     * Erases the region marked in [mask] from [source] and fills it back in, mutating
     * [source] in place. [source] is the caller-owned, already-decoded working bitmap -
     * this call never allocates a second full-size bitmap.
     */
    suspend fun inpaint(
        source: Bitmap,
        mask: MaskRegion,
        onProgress: (InpaintingStage) -> Unit,
    ): Result<InpaintingResult>
}
