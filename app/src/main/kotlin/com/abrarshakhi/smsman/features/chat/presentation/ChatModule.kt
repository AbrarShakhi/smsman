package com.abrarshakhi.smsman.features.chat.presentation

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    viewModel { (threadId: Long) ->
        ChatViewModel(
            repository = get(),
            metadata = get(),
            titleResolver = get(),
            sender = get(),
            messages = get(),
            notifier = get(),
            threadId = threadId,
        )
    }
}
