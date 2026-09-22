package com.abrarshakhi.smsman.features.chat.presentation

import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val chatModule = module {
    viewModel { (threadId: Long) -> ChatViewModel(get(), get(), threadId) }

    navigation<AppRouteKey.Chat> { route ->
        ChatScreen(
            viewModel = koinViewModel(key = "chat-${route.threadId}") {
                parametersOf(route.threadId)
            },
        )
    }
}
