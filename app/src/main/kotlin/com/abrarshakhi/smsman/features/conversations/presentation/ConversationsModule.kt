package com.abrarshakhi.smsman.features.conversations.presentation

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val conversationsModule = module {
    viewModel { (favoritesOnly: Boolean) -> ConversationsViewModel(get(), favoritesOnly) }
}
