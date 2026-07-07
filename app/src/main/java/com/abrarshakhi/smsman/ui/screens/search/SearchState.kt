package com.abrarshakhi.smsman.ui.screens.search

import com.abrarshakhi.smsman.domain.model.SearchHit

data class SearchState(
    val query: String = "",
    val hits: List<SearchHit> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
)
