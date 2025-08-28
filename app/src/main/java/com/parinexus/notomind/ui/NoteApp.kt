package com.parinexus.notomind.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.navOptions
import com.parinexus.designsystem.component.NoteBackground
import com.parinexus.designsystem.component.NoteGradientBackground
import com.parinexus.designsystem.icon.NoteIcon
import com.parinexus.designsystem.theme.GradientColors
import com.parinexus.designsystem.theme.LocalGradientColors
import com.parinexus.detail.navigation.DetailArg
import com.parinexus.detail.navigation.navigateToDetail
import com.parinexus.labelscreen.navigateToLabel
import com.parinexus.main.navigation.navigateToMain
import com.parinexus.notomind.MainActivityViewModel
import com.parinexus.notomind.navigation.NoteNavHost
import com.parinexus.setting.navigation.navigateToSetting

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteApp(
    viewModel: MainActivityViewModel,
    appState: NoteAppState,
    modifier: Modifier = Modifier,
) {
    val shouldShowGradientBackground = true
    val labels = viewModel.labels.collectAsStateWithLifecycle()

    NoteBackground(modifier = modifier) {
        NoteGradientBackground(
            gradientColors = if (shouldShowGradientBackground) {
                LocalGradientColors.current
            } else {
                GradientColors()
            },
        ) {
            val snackbarHostState = remember { SnackbarHostState() }

            val isOffline by appState.isOffline.collectAsStateWithLifecycle()

            val notConnectedMessage = "not connected" // stringResource(R.string.not_connected)
            LaunchedEffect(isOffline) {
                if (isOffline) {
                    snackbarHostState.showSnackbar(
                        message = notConnectedMessage,
                        duration = Indefinite,
                    )
                }
            }
            ModalNavigationDrawer(
                drawerContent = {
                    MainNavigation(
                        labels = labels.value,
                        currentMainArg = appState.mainArg,
                        onNavigation = {
                            appState.navController.popBackStack()
                            appState.navController.navigateToMain(
                                it,
                                navOptions {
                                    //  this.launchSingleTop=true
                                },
                            )
                            appState.closeDrawer()
                        },
                        navigateToLevel = {
                            appState.navController.navigateToLabel(it)
                            appState.closeDrawer()
                        },
                        navigateToSetting = {
                            appState.navController.navigateToSetting()
                            appState.closeDrawer()
                        },
                        )
                },
                drawerState = appState.drawerState,
                gesturesEnabled = appState.isMain,
            ) {
                Scaffold(
                    modifier = modifier.semantics {
                        testTagsAsResourceId = true
                    },
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (appState.isMain) {
                            NoteBottomBar(
                                onAddNewNote = {
                                    appState.navController.navigateToDetail(DetailArg(-1))
                                },
                            )
                        }
                    },

                    ) { padding ->
                    NoteNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(
                                    WindowInsetsSides.Horizontal,
                                ),
                            ),
                        appState = appState,
                    )
                }
            }
        }
    }
}

@Composable
fun NoteBottomBar(
    onAddNewNote: () -> Unit = {},
) {
    BottomAppBar(
        actions = {
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewNote,
                shape = RoundedCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(),
            ) {
                Icon(imageVector = NoteIcon.Add, contentDescription = "add note")
            }
        },
    )
}
