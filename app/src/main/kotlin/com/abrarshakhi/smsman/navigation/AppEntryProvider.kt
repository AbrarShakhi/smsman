package com.abrarshakhi.smsman.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.abrarshakhi.smsman.ui.chat.ChatRoute
import com.abrarshakhi.smsman.ui.conversations.ConversationsRoute
import com.abrarshakhi.smsman.ui.newmessage.NewMessageRoute
import com.abrarshakhi.smsman.ui.pinned.PinnedRoute
import com.abrarshakhi.smsman.ui.search.SearchRoute
import com.abrarshakhi.smsman.ui.settings.SettingsRoute

fun appEntryProvider(navigator: Navigator): (NavKey) -> NavEntry<NavKey> = entryProvider {
    entry<AppRouteKey.AllMessages> {
        ConversationsRoute(
            favoritesOnly = false,
            onOpenConversation = { threadId -> navigator.navigate(AppRouteKey.Chat(threadId)) },
            onOpenSearch = { navigator.navigate(AppRouteKey.Search) },
            onOpenSettings = { navigator.navigate(AppRouteKey.Settings) },
            onStartChat = { navigator.navigate(AppRouteKey.NewMessage) },
        )
    }

    entry<AppRouteKey.Favorite> {
        ConversationsRoute(
            favoritesOnly = true,
            onOpenConversation = { threadId -> navigator.navigate(AppRouteKey.Chat(threadId)) },
            onOpenSearch = { navigator.navigate(AppRouteKey.Search) },
            onOpenSettings = { navigator.navigate(AppRouteKey.Settings) },
        )
    }

    entry<AppRouteKey.Pinned> {
        PinnedRoute(
            onOpenMessage = { threadId, messageId ->
                navigator.navigate(AppRouteKey.Chat(threadId, highlightMessageId = messageId))
            },
            onOpenSearch = { navigator.navigate(AppRouteKey.Search) },
            onOpenSettings = { navigator.navigate(AppRouteKey.Settings) },
        )
    }

    entry<AppRouteKey.Chat> { key ->
        ChatRoute(
            threadId = key.threadId,
            highlightMessageId = key.highlightMessageId,
            onBack = navigator::goBack,
        )
    }

    entry<AppRouteKey.NewMessage> {
        NewMessageRoute(
            onBack = navigator::goBack,
            onMessageSent = { threadId ->
                navigator.goBack()
                navigator.navigate(AppRouteKey.Chat(threadId))
            },
        )
    }

    entry<AppRouteKey.Search> {
        SearchRoute(
            onBack = navigator::goBack,
            onOpenMessage = { threadId, messageId ->
                navigator.navigate(AppRouteKey.Chat(threadId, highlightMessageId = messageId))
            },
            onStartChat = { navigator.navigate(AppRouteKey.NewMessage) },
        )
    }

    entry<AppRouteKey.Settings> {
        SettingsRoute(onBack = navigator::goBack)
    }
}
