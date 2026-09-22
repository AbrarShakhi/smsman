package com.abrarshakhi.smsman.features.newmessage.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.abrarshakhi.smsman.common.main.ScreenChrome
import com.abrarshakhi.smsman.common.main.BackNavigationIcon

fun newMessageChrome() = ScreenChrome(
    title = "New message",
    topBar = { backStack, scrollBehavior ->
        TopAppBar(
            title = { Text("New message") },
            navigationIcon = { BackNavigationIcon(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
)
