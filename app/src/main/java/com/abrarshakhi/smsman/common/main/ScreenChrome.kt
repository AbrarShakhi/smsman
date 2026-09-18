package com.abrarshakhi.smsman.common.main

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.features.chat.presentation.chatChrome
import com.abrarshakhi.smsman.features.home.presentation.homeChrome
import com.abrarshakhi.smsman.features.onboarding.presentation.onboardingChrome
import com.abrarshakhi.smsman.features.settings.presentation.settingsChrome

data class ScreenChrome(
    val title: String,
    val topBar: @Composable (
        backStack: SnapshotStateList<AppRouteKey>,
        scrollBehavior: TopAppBarScrollBehavior,
    ) -> Unit = { _, _ -> },
    val fab: @Composable (backStack: SnapshotStateList<AppRouteKey>) -> Unit = {},
)

fun AppRouteKey.chrome() = when (this) {
    is AppRouteKey.Onboarding -> onboardingChrome()
    is AppRouteKey.Home -> homeChrome()
    is AppRouteKey.Chat -> chatChrome()
    is AppRouteKey.Settings -> settingsChrome()
}

