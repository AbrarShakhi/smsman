package com.abrarshakhi.smsman.features.pinned.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.main.AppBottomBar
import com.abrarshakhi.smsman.common.main.ScreenChrome
import com.abrarshakhi.smsman.features.conversations.presentation.OverflowMenu
import com.abrarshakhi.smsman.features.conversations.presentation.SearchAction

fun pinnedChrome() = ScreenChrome(
    title = "Pinned",
    topBar = { backStack, scrollBehavior ->
        TopAppBar(
            title = { Text(stringResource(R.string.tab_pinned)) },
            actions = { SearchAction(backStack); OverflowMenu(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
)
