package com.abrarshakhi.smsman.features.conversations.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.main.AppBottomBar
import com.abrarshakhi.smsman.common.main.ScreenChrome
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.navigateTo

fun allMessagesChrome() = ScreenChrome(
    title = "Messages",
    topBar = { backStack, scrollBehavior ->
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.tab_all_messages),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.headlineMediumEmphasized
                )
            },
            actions = { SearchAction(backStack); OverflowMenu(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
    fab = { backStack ->
        ExtendedFloatingActionButton(
            onClick = { backStack.navigateTo(AppRouteKey.NewMessage) },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Message,
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
            title = {
                Text(
                    stringResource(R.string.tab_favorite),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.headlineMediumEmphasized
                )
            },
            actions = { SearchAction(backStack); OverflowMenu(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
)

@Composable
fun SearchAction(backStack: SnapshotStateList<AppRouteKey>) {
    IconButton(onClick = { backStack.navigateTo(AppRouteKey.Search) }) {
        Icon(Icons.Filled.Search, contentDescription = "Search")
    }
}

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
