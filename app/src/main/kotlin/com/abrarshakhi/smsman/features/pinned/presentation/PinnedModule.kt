package com.abrarshakhi.smsman.features.pinned.presentation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val pinnedModule = module {
    viewModelOf(::PinnedViewModel)
}
