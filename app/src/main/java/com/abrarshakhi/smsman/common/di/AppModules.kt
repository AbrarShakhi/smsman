package com.abrarshakhi.smsman.common.di

import com.abrarshakhi.smsman.common.main.MainAppViewModel
import com.abrarshakhi.smsman.core.di.coreModule
import com.abrarshakhi.smsman.features.chat.presentation.chatModule
import com.abrarshakhi.smsman.features.conversations.presentation.conversationsModule
import com.abrarshakhi.smsman.features.newmessage.presentation.newMessageModule
import com.abrarshakhi.smsman.features.onboarding.presentation.onboardingModule
import com.abrarshakhi.smsman.features.pinned.presentation.pinnedModule
import com.abrarshakhi.smsman.features.search.presentation.searchModule
import com.abrarshakhi.smsman.features.settings.presentation.settingsModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val commonModule = module {
    viewModelOf(::MainAppViewModel)
}

val appModules = listOf(
    commonModule,
    coreModule,
    onboardingModule,
    conversationsModule,
    pinnedModule,
    chatModule,
    newMessageModule,
    searchModule,
    settingsModule,
)
