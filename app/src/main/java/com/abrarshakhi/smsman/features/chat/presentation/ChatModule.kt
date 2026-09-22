package com.abrarshakhi.smsman.features.chat.presentation

import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val chatModule = module {
    navigation<AppRouteKey.Chat> { route -> ChatScreen(chatId = route.chatId) }
}
