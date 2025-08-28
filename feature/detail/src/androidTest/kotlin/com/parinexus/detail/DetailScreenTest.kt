package com.parinexus.detail

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.parinexus.model.NotoMind
import org.junit.Rule
import org.junit.Test

class DetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Test
    fun loading_showsLoadingSpinner() {
        composeTestRule.setContent {
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

        composeTestRule
            .onNodeWithTag("detail:content", true)
            .assertExists()

        composeTestRule
            .onNodeWithTag("detail:title", true)
            .assertExists()
    }
}
