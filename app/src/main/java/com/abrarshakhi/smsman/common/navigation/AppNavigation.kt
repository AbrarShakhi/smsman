package com.abrarshakhi.smsman.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.smsman.common.main.MainAppViewModel
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavigation(
    backStack: SnapshotStateList<AppRouteKey>,
    modifier: Modifier = Modifier,
    mainAppViewModel: MainAppViewModel,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        // NavDisplay only installs its back handler while previous entries exist, so back at the
        // root falls through to the system and exits the app. Routed through back() anyway so every
        // pop in the app goes through the same size-guarded helper.
        onBack = { backStack.back() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = koinEntryProvider<AppRouteKey>(),
    )
}
