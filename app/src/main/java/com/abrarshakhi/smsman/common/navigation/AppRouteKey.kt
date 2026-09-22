package com.abrarshakhi.smsman.common.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey : NavKey {
    @Serializable
    data object Onboarding : AppRouteKey

    /** Bottom-bar destinations. Switched with [switchTapTo], so each sits at back-stack depth 1. */
    @Serializable
    sealed interface HomeTab : AppRouteKey

    @Serializable
    data object AllMessages : HomeTab

    @Serializable
    data object Favorite : HomeTab

    @Serializable
    data object Pinned : HomeTab

    @Serializable
    data class Chat(
        val threadId: Long,
        /** Set when arriving from the Pinned tab, so the thread can scroll to that message. */
        val highlightMessageId: Long? = null,
    ) : AppRouteKey

    @Serializable
    data object NewMessage : AppRouteKey

    @Serializable
    data object Search : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey
}
