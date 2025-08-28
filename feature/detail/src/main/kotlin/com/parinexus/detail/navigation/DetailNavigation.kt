package com.parinexus.detail.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.parinexus.detail.DetailRoute

fun NavController.navigateToDetail(detailArg: DetailArg, navOptions: NavOptions = navOptions { }) = navigate(detailArg, navOptions)

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.detailScreen(
    onBack: () -> Unit,
    navigateToGallery: (Long) -> Unit,
    navigateToSelectLevel: (Set<Long>) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier.Companion,
) {
    composable<DetailArg> {
        DetailRoute(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this,
            onBack = onBack,
            navigateToGallery = navigateToGallery,
            navigateToSelectLevel = navigateToSelectLevel,
        )
    }
}
