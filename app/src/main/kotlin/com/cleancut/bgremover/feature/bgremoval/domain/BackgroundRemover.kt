package com.cleancut.bgremover.feature.bgremoval.domain

import android.net.Uri

enum class BackgroundRemovalStage {
    DECODING,
    CHECKING_MODEL,
    DOWNLOADING_MODEL,
    SEGMENTING,
    COMPOSITING,
    DONE,
}

sealed class BackgroundRemovalError : Exception() {
    /** Device has no usable Google Play Services install at all. */
    object PlayServicesUnavailable : BackgroundRemovalError()

    /** The on-demand ML Kit module failed to download (e.g. no network on first use). */
    object ModuleDownloadFailed : BackgroundRemovalError()

    object DecodeFailed : BackgroundRemovalError()

    /** Compositing ran out of memory on this device for this image size. */
    object OutOfMemory : BackgroundRemovalError()

    data class SegmentationFailed(override val cause: Throwable) : BackgroundRemovalError()
}

interface BackgroundRemover {
    suspend fun removeBackground(
        sourceUri: Uri,
        onProgress: (BackgroundRemovalStage) -> Unit,
    ): Result<CompositeResult>
}
