package com.parinexus.notomind.ui

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.parinexus.data.util.NetworkMonitor
import com.parinexus.main.navigation.MainRoute
import com.parinexus.main.navigation.TypeArg
import com.parinexus.model.NoteType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@Composable
fun rememberNoteAppState(
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
): NoteAppState {
    return remember(
        navController,
        coroutineScope,
        networkMonitor,
        drawerState,
    ) {
        NoteAppState(
            navController = navController,
            coroutineScope = coroutineScope,
            networkMonitor = networkMonitor,
            drawerState = drawerState,
        )
    }
}

@Stable
class NoteAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
    val drawerState: DrawerState,
) {
    val currentRoute: String
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination?.route ?: ""

    val mainArg: Long
        @Composable get() = navController.currentBackStackEntryAsState().value?.arguments?.getLong(
            TypeArg,
        ) ?: NoteType.NOTE.index

    //            navController
//            .currentBackStackEntryAsState().value?.toRoute<Main>() ?:
    // Main(-1L)
    val isMain: Boolean
        @Composable get() = currentRoute.contains(MainRoute)

    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun closeDrawer() {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    fun openDrawer() {
        coroutineScope.launch {
            drawerState.open()
        }
    }
}

val <T : Any> KClass<T>.name: String
    get() {
        return this.qualifiedName.toString()
    }
