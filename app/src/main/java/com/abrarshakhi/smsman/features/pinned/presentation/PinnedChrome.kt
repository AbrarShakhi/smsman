package com.abrarshakhi.smsman.features.pinned.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.main.AppBottomBar
import com.abrarshakhi.smsman.common.main.ScreenChrome

fun pinnedChrome() = ScreenChrome(
    title = "Pinned",
    topBar = { _, scrollBehavior ->
        TopAppBar(
            title = { Text(stringResource(R.string.tab_pinned)) },
            scrollBehavior = scrollBehavior,
        )
    },
    bottomBar = { backStack -> AppBottomBar(backStack) },
)
