package com.abrarshakhi.smsman.features.conversations.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.main.AppBottomBar
import com.abrarshakhi.smsman.common.main.ScreenChrome
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.navigateTo

/**
 * All messages is the only destination with a FAB. Favorite and Pinned deliberately leave
 * [ScreenChrome.fab] at its empty default.
 */
fun allMessagesChrome() = ScreenChrome(
    title = "Messages",
    topBar = { backStack, scrollBehavior ->
        TopAppBar(
            title = { Text(stringResource(R.string.tab_all_messages)) },
            actions = { OverflowMenu(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
    fab = { backStack ->
        ExtendedFloatingActionButton(
            onClick = { backStack.navigateTo(AppRouteKey.NewMessage) },
            icon = {
                Icon(
                    painterResource(R.drawable.ic_tab_messages),
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(R.string.action_start_chat)) },
        )
    },
)

fun favoriteChrome() = ScreenChrome(
    title = "Favorite",
    topBar = { backStack, scrollBehavior ->
        TopAppBar(
            title = { Text(stringResource(R.string.tab_favorite)) },
            actions = { OverflowMenu(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
)

/** Shared overflow for the tab destinations. */
@Composable
fun OverflowMenu(backStack: SnapshotStateList<AppRouteKey>, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }, modifier = modifier) {
        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                open = false
                backStack.navigateTo(AppRouteKey.Settings)
            },
        )
    }
}
