package com.cleancut.bgremover.feature.bgremoval.domain

import android.net.Uri

enum class BackgroundRemovalStage {
    DECODING,
    LOADING_MODEL,
    SEGMENTING,
    COMPOSITING,
    DONE,
}

sealed class BackgroundRemovalError : Exception() {
    object DecodeFailed : BackgroundRemovalError()

    /** Compositing ran out of memory on this device for this image size. */
    object OutOfMemory : BackgroundRemovalError()

    /** The bundled ONNX model asset failed to load into an inference session. */
    data class ModelLoadFailed(override val cause: Throwable) : BackgroundRemovalError()

    data class SegmentationFailed(override val cause: Throwable) : BackgroundRemovalError()
}

interface BackgroundRemover {
    suspend fun removeBackground(
        sourceUri: Uri,
        onProgress: (BackgroundRemovalStage) -> Unit,
    ): Result<CompositeResult>
}
