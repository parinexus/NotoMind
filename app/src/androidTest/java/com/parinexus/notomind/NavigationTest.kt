package com.parinexus.notomind

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.parinexus.testing.rules.GrantPostNotificationsPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.properties.ReadOnlyProperty

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @get:Rule(order = 1)
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @get:Rule(order = 2)
    val postNotificationsPermission = GrantPostNotificationsPermissionRule()

    @get:Rule(order = 3)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun AndroidComposeTestRule<*, *>.stringResource(@StringRes resId: Int) =
        ReadOnlyProperty<Any, String> { _, _ -> activity.getString(resId) }

    @Before
    fun setup() = hiltRule.inject()

    @Test
    fun firstScreen_isMain() {
        composeTestRule.apply {
            // VERIFY for you is selected
            onNodeWithText("Add Note").assertExists()
        }
    }

    @Test
    fun onAddButton_showDetails() {
        composeTestRule.apply {
            // GIVEN the user follows a topic
            onNodeWithText("Add Note").performClick()

            onNodeWithTag("detail:title").assertExists()
        }
    }
}
