package com.abrarshakhi.smsman.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey : NavKey {

    @Serializable
    data object AllMessages : AppRouteKey

    @Serializable
    data object Favorite : AppRouteKey

    @Serializable
    data object Pinned : AppRouteKey

    @Serializable
    data class Chat(
        val threadId: Long,
        val highlightMessageId: Long? = null,
    ) : AppRouteKey

    @Serializable
    data object NewMessage : AppRouteKey

    @Serializable
    data object Search : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey
}
