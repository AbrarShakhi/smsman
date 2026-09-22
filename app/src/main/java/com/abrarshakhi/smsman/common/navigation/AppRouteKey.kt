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
    ) : AppRouteKey

    @Serializable
    data object NewMessage : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey
}
