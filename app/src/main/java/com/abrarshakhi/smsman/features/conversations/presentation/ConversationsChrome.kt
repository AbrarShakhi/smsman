package com.abrarshakhi.smsman.features.conversations.presentation

import androidx.compose.material3.ExtendedFloatingActionButton
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
    topBar = { _, scrollBehavior ->
        TopAppBar(
            title = { Text(stringResource(R.string.tab_all_messages)) },
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
    topBar = { _, scrollBehavior ->
        TopAppBar(
            title = { Text(stringResource(R.string.tab_favorite)) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
)
