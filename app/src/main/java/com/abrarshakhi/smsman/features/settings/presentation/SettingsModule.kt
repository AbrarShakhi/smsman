package com.abrarshakhi.smsman.features.settings.presentation

import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val settingsModule = module {
    viewModelOf(::SettingsViewModel)

    navigation<AppRouteKey.Settings> { SettingsScreen(viewModel = koinViewModel()) }
}
