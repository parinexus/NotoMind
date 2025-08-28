package com.parinexus.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.parinexus.model.NotoMind
import com.parinexus.testing.util.Capture

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun DetailScreenShot() {
    Capture {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                EditScreen(
                    notoMind = NotoMind(),
                    title = TextFieldState(),
                    content = TextFieldState(),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this,,
                )
            }
        }
    }
}
