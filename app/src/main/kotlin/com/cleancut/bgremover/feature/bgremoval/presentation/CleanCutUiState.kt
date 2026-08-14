package com.cleancut.bgremover.feature.bgremoval.presentation

import android.graphics.Bitmap
import com.cleancut.bgremover.feature.bgremoval.domain.BackgroundRemovalStage

sealed interface CleanCutUiState {
    data object Idle : CleanCutUiState

    data class Processing(val stage: BackgroundRemovalStage) : CleanCutUiState

    data class Result(val bitmap: Bitmap, val savedMessage: String? = null) : CleanCutUiState

    data class Error(val message: String, val recoverable: Boolean) : CleanCutUiState
}
