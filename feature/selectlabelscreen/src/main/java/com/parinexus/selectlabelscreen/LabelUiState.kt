package com.parinexus.selectlabelscreen

import androidx.compose.ui.state.ToggleableState
import com.parinexus.model.Label

data class LabelUiState(
    val id: Long,
    val label: String,
    val toggleableState: ToggleableState = ToggleableState.Off,
)

fun Label.toLabelUiState() = LabelUiState(this.id, this.label)
