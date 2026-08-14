package com.cleancut.bgremover.ui

import android.graphics.Bitmap
import com.cleancut.bgremover.segmentation.BackgroundRemovalStage

sealed interface CleanCutUiState {
    data object Idle : CleanCutUiState

    data class Processing(val stage: BackgroundRemovalStage) : CleanCutUiState

    data class Result(val bitmap: Bitmap, val savedMessage: String? = null) : CleanCutUiState

    data class Error(val message: String, val recoverable: Boolean) : CleanCutUiState
}
