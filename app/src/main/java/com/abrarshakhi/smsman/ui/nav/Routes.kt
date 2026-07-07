package com.abrarshakhi.smsman.ui.nav

/**
 * Navigation 3 keys. Plain data classes/objects — Nav3 uses `Any` as its back-stack
 * element type and matches entries by class via `entry<T> { … }`.
 *
 * Routes carry arguments as constructor params; the entry receives the route instance.
 */

data object OnboardingRoute

data object ConversationListRoute

data class ChatRoute(val threadId: Long)

data class ComposeNewRoute(val initialRecipient: String? = null)

data object SettingsRoute

data object SearchRoute

data class SearchInThreadRoute(val threadId: Long)

data object BlockedNumbersRoute

data object BackupRestoreRoute

data object ScheduledMessagesRoute
