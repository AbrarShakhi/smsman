package com.abrarshakhi.smsman.navigation

import androidx.navigation3.runtime.NavKey
import com.abrarshakhi.smsman.model.AppDocument
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

    @Serializable
    data class Document(val document: AppDocument) : AppRouteKey
}
