package com.abrarshakhi.smsman.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.smsman.ui.screens.backup.BackupRestoreScreen
import com.abrarshakhi.smsman.ui.screens.blocked.BlockedNumbersScreen
import com.abrarshakhi.smsman.ui.screens.compose.ComposeNewScreen
import com.abrarshakhi.smsman.ui.screens.chat.ChatScreen
import com.abrarshakhi.smsman.ui.screens.scheduled.ScheduledMessagesScreen
import com.abrarshakhi.smsman.ui.screens.conversations.ConversationListScreen
import com.abrarshakhi.smsman.ui.screens.search.SearchInThreadScreen
import com.abrarshakhi.smsman.ui.screens.search.SearchScreen
import com.abrarshakhi.smsman.ui.screens.settings.SettingsScreen
import com.abrarshakhi.smsman.ui.screens.onboarding.OnboardingScreen

@Composable
fun AppNavHost(startKey: Any = ConversationListRoute) {
    val backStack = remember { mutableStateListOf<Any>(startKey) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<OnboardingRoute> {
                OnboardingScreen(
                    onComplete = {
                        backStack.clear()
                        backStack.add(ConversationListRoute)
                    },
                )
            }

            entry<ConversationListRoute> {
                ConversationListScreen(
                    onConversationClick = { id -> backStack.add(ChatRoute(id)) },
                    onComposeClick = { backStack.add(ComposeNewRoute()) },
                    onSettingsClick = { backStack.add(SettingsRoute) },
                    onSearchClick = { backStack.add(SearchRoute) },
                )
            }

            entry<ChatRoute> { key ->
                ChatScreen(
                    threadId = key.threadId,
                    onBack = { backStack.removeLastOrNull() },
                    onSearchInThread = { backStack.add(SearchInThreadRoute(key.threadId)) },
                )
            }

            entry<ComposeNewRoute> { key ->
                ComposeNewScreen(
                    initialRecipient = key.initialRecipient,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToChat = { threadId ->
                        backStack.removeLastOrNull()
                        backStack.add(ChatRoute(threadId))
                    },
                )
            }

            entry<SettingsRoute> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onBlockedNumbers = { backStack.add(BlockedNumbersRoute) },
                    onBackup = { backStack.add(BackupRestoreRoute) },
                    onScheduled = { backStack.add(ScheduledMessagesRoute) },
                )
            }

            entry<SearchRoute> {
                SearchScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onResultClick = { threadId ->
                        backStack.removeLastOrNull()           // pop search
                        backStack.add(ChatRoute(threadId))
                    },
                )
            }

            entry<SearchInThreadRoute> { key ->
                SearchInThreadScreen(
                    threadId = key.threadId,
                    onBack = { backStack.removeLastOrNull() },
                )
            }

            entry<BlockedNumbersRoute> {
                BlockedNumbersScreen(onBack = { backStack.removeLastOrNull() })
            }

            entry<BackupRestoreRoute> {
                BackupRestoreScreen(onBack = { backStack.removeLastOrNull() })
            }

            entry<ScheduledMessagesRoute> {
                ScheduledMessagesScreen(onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
