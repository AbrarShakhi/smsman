package com.abrarshakhi.smsman.common.main

import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.features.chat.presentation.chatChrome
import com.abrarshakhi.smsman.features.conversations.presentation.allMessagesChrome
import com.abrarshakhi.smsman.features.conversations.presentation.favoriteChrome
import com.abrarshakhi.smsman.features.newmessage.presentation.newMessageChrome
import com.abrarshakhi.smsman.features.onboarding.presentation.onboardingChrome
import com.abrarshakhi.smsman.features.pinned.presentation.pinnedChrome
import com.abrarshakhi.smsman.features.settings.presentation.settingsChrome

data class ScreenChrome(
    val title: String,
    val topBar: @Composable (
        backStack: SnapshotStateList<AppRouteKey>,
        scrollBehavior: TopAppBarScrollBehavior,
    ) -> Unit = { _, _ -> },
    val bottomBar: @Composable (backStack: SnapshotStateList<AppRouteKey>) -> Unit = {},
    val fab: @Composable (backStack: SnapshotStateList<AppRouteKey>) -> Unit = {},
)

/**
 * Exhaustive by design: adding an [AppRouteKey] fails to compile until its chrome is declared here.
 * This is the compile-time counterpart to the Koin `navigation<T> {}` registration, which only
 * fails at navigation time.
 */
fun AppRouteKey.chrome() = when (this) {
    is AppRouteKey.Onboarding -> onboardingChrome()
    is AppRouteKey.AllMessages -> allMessagesChrome()
    is AppRouteKey.Favorite -> favoriteChrome()
    is AppRouteKey.Pinned -> pinnedChrome()
    is AppRouteKey.Chat -> chatChrome(threadId)
    is AppRouteKey.NewMessage -> newMessageChrome()
    is AppRouteKey.Settings -> settingsChrome()
}
