package com.parinexus.main.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.parinexus.main.MainRoute
import com.parinexus.model.NoteType

fun NavController.navigateToMain(
    type: Long,
    navOptions: NavOptions = navOptions { },
) = navigate(route = "$MainRoute/$type", navOptions)

const val MainRoute = "main"
const val TypeArg = "mainArg"
const val FullMainRoute = "$MainRoute/{$TypeArg}"

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    navigateToDetail: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    ) {
    composable(
        route = FullMainRoute,
        arguments = listOf(
            navArgument(TypeArg) {
                type = NavType.LongType
                defaultValue = -1L
            },
        ),
    ) {
        MainRoute(
            modifier = modifier,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this,
            navigateToDetail = navigateToDetail,
            onOpenDrawer = onOpenDrawer,
        )
    }
}

internal class MainArg(val type: Long) {
    val noteType: NoteType = when (type) {
        NoteType.NOTE.index -> NoteType.NOTE
        NoteType.ARCHIVE.index -> NoteType.ARCHIVE
        NoteType.REMAINDER.index -> NoteType.REMAINDER
        else -> NoteType.LABEL
    }

    constructor(savedStateHandle: SavedStateHandle) :
        this(
            type = checkNotNull(savedStateHandle[TypeArg]),

        )
}
//
