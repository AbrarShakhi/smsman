package com.abrarshakhi.smsman.features.newmessage.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val newMessageModule = module {
    viewModelOf(::NewMessageViewModel)
}
