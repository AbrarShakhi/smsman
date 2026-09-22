package com.abrarshakhi.smsman.features.search.presentation

import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import org.koin.compose.viewmodel.koinActivityViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val searchModule = module {
    viewModelOf(::SearchViewModel)

    // Activity-scoped so the top-bar field and these results share one instance.
    navigation<AppRouteKey.Search> { SearchScreen(viewModel = koinActivityViewModel()) }
}
