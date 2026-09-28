package com.abrarshakhi.smsman.features.settings.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.abrarshakhi.smsman.common.main.BackNavigationIcon
import com.abrarshakhi.smsman.common.main.ScreenChrome

fun settingsChrome() = ScreenChrome(
    title = "Settings",
    topBar = { backStack, scrollBehavior ->
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = { BackNavigationIcon(backStack) },
            scrollBehavior = scrollBehavior,
        )
    },
)
