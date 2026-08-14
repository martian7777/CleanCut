package com.cleancut.bgremover.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cleancut.bgremover.segmentation.BackgroundRemovalStage

@Composable
fun ProcessingIndicator(stage: BackgroundRemovalStage, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        Text(text = stageLabel(stage), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun stageLabel(stage: BackgroundRemovalStage): String = when (stage) {
    BackgroundRemovalStage.DECODING -> "Reading photo…"
    BackgroundRemovalStage.CHECKING_MODEL -> "Checking AI model…"
    BackgroundRemovalStage.DOWNLOADING_MODEL -> "Downloading AI model (first use)…"
    BackgroundRemovalStage.SEGMENTING -> "Finding the subject…"
    BackgroundRemovalStage.COMPOSITING -> "Removing background…"
    BackgroundRemovalStage.DONE -> "Done"
}
