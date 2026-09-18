package com.abrarshakhi.smsman.common.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey : NavKey {
    @Serializable
    data object Onboarding : AppRouteKey

    @Serializable
    data object Home : AppRouteKey

    @Serializable
    data class Chat(
        val chatId: Int // Replace with chat ID which will be better.
    ) : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey
}