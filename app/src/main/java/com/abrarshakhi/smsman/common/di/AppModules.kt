package com.abrarshakhi.smsman.common.di

import com.abrarshakhi.smsman.common.main.MainAppViewModel
import com.abrarshakhi.smsman.features.chat.presentation.chatModule
import com.abrarshakhi.smsman.features.conversations.presentation.conversationsModule
import com.abrarshakhi.smsman.features.newmessage.presentation.newMessageModule
import com.abrarshakhi.smsman.features.onboarding.presentation.onboardingModule
import com.abrarshakhi.smsman.features.pinned.presentation.pinnedModule
import com.abrarshakhi.smsman.features.settings.presentation.settingsModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val commonModule = module {
    viewModelOf(::MainAppViewModel)
}

/**
 * Every feature module registers its own nav entries via `navigation<AppRouteKey.X> { }`.
 * A route missing from this list resolves to Koin's default fallback, which throws
 * `IllegalStateException("Unknown screen ...")` at navigation time rather than at compile time.
 */
val appModules = listOf(
    commonModule,
    onboardingModule,
    conversationsModule,
    pinnedModule,
    chatModule,
    newMessageModule,
    settingsModule,
)
