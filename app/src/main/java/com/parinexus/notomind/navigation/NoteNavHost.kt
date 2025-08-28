package com.parinexus.notomind.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.parinexus.detail.navigation.DetailArg
import com.parinexus.detail.navigation.detailScreen
import com.parinexus.detail.navigation.navigateToDetail
import com.parinexus.gallery.GalleryArg
import com.parinexus.gallery.galleryScreen
import com.parinexus.gallery.navigateToGallery
import com.parinexus.labelscreen.labelScreen
import com.parinexus.main.navigation.FullMainRoute
import com.parinexus.main.navigation.mainScreen
import com.parinexus.notomind.ui.NoteAppState
import com.parinexus.selectlabelscreen.navigateToSelectLabel
import com.parinexus.selectlabelscreen.selectLabelScreen
import com.parinexus.setting.navigation.settingScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NoteNavHost(
    appState: NoteAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController
    SharedTransitionLayout(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = FullMainRoute,
            modifier = Modifier,
        ) {
            mainScreen(
                modifier = Modifier,
                sharedTransitionScope = this@SharedTransitionLayout,
                navigateToDetail = { navController.navigateToDetail(DetailArg(it)) },
                onOpenDrawer = {
                    appState.openDrawer()
                }
            )
            detailScreen(
                modifier = Modifier,
                sharedTransitionScope = this@SharedTransitionLayout,
                onBack = navController::popBackStack,
                navigateToGallery = { navController.navigateToGallery(GalleryArg(it)) },
                navigateToSelectLevel = appState.navController::navigateToSelectLabel,
            )
            galleryScreen(
                onBack = navController::popBackStack,
            )
            labelScreen(onBack = navController::popBackStack)
            selectLabelScreen(onBack = navController::popBackStack)
            settingScreen(
                modifier = Modifier,
                onBack = navController::popBackStack,
            )
        }
    }
}