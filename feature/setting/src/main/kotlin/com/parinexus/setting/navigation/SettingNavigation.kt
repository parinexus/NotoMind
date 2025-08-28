package com.parinexus.setting.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.dialog
import androidx.navigation.navOptions
import com.parinexus.setting.SettingRoute

fun NavController.navigateToSetting(navOptions: NavOptions = navOptions { }) = navigate(
    Setting,
    navOptions,
)

fun NavGraphBuilder.settingScreen(
    modifier: Modifier,
    onBack: () -> Unit,
) {
    dialog<Setting> {
        SettingRoute(
            modifier = modifier,
            onBack = onBack,
        )
    }
}
