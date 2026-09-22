package com.abrarshakhi.smsman.features.conversations.presentation

import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val conversationsModule = module {
    navigation<AppRouteKey.AllMessages> { ConversationsScreen(favoritesOnly = false) }
    navigation<AppRouteKey.Favorite> { ConversationsScreen(favoritesOnly = true) }
}
