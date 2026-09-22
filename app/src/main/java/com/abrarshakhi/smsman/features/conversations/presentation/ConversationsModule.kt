package com.abrarshakhi.smsman.features.conversations.presentation

import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val conversationsModule = module {
    viewModel { (favoritesOnly: Boolean) -> ConversationsViewModel(get(), favoritesOnly) }

    navigation<AppRouteKey.AllMessages> {
        ConversationsScreen(
            viewModel = koinViewModel(key = "all") { parametersOf(false) },
            favoritesOnly = false,
        )
    }
    navigation<AppRouteKey.Favorite> {
        ConversationsScreen(
            viewModel = koinViewModel(key = "favorite") { parametersOf(true) },
            favoritesOnly = true,
        )
    }
}
