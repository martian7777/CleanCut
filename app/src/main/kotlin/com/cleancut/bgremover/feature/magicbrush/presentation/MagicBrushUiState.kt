package com.cleancut.bgremover.feature.magicbrush.presentation

import android.graphics.Bitmap
import com.cleancut.bgremover.feature.magicbrush.domain.InpaintingStage

sealed interface MagicBrushUiState {
    data object Idle : MagicBrushUiState

    /**
     * Deliberate divergence from CleanCutUiState's full-state-swap-per-operation pattern:
     * Magic Brush needs to stay on the same canvas across many erase steps (paint A, erase,
     * evaluate, paint B, erase...), so bouncing to a separate full-screen Processing state
     * and back after every single erase would lose canvas zoom/pan continuity. Instead,
     * "processing" is [erasingStage], rendered as a small overlay on the canvas.
     *
     * [revision] increments on every mutation (erase/undo/redo). [workingBitmap] is mutated
     * in place - its object reference never changes across steps - so UI code must key
     * `asImageBitmap()`/recomposition off [revision], not the Bitmap reference, or a
     * completed erase can silently fail to redraw.
     *
     * A failed erase surfaces as [errorMessage] (transient, cleared on the next erase)
     * rather than the top-level [Error] state, so one failed attempt doesn't discard the
     * canvas or undo history. [Error] is reserved for failures before any [Editing] state
     * exists (e.g. the initial image decode).
     */
    data class Editing(
        val workingBitmap: Bitmap,
        val originalBitmap: Bitmap,
        val revision: Int = 0,
        val brushSizePx: Float = DEFAULT_BRUSH_SIZE_PX,
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val erasingStage: InpaintingStage? = null,
        val savedMessage: String? = null,
        val errorMessage: String? = null,
    ) : MagicBrushUiState

    data class Error(val message: String, val recoverable: Boolean) : MagicBrushUiState

    companion object {
        const val DEFAULT_BRUSH_SIZE_PX = 60f
    }
}
