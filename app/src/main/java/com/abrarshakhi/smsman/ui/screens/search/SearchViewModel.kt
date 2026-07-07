package com.abrarshakhi.smsman.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.length < MIN_QUERY) {
                        _state.update { it.copy(hits = emptyList(), isSearching = false, hasSearched = false) }
                        return@collect
                    }
                    _state.update { it.copy(isSearching = true) }
                    val hits = searchRepository.searchMessages(query)
                    _state.update { it.copy(hits = hits, isSearching = false, hasSearched = true) }
                }
        }
    }

    fun onQueryChange(text: String) {
        _state.update { it.copy(query = text) }
        queryFlow.value = text
    }

    private companion object {
        const val DEBOUNCE_MS = 250L
        const val MIN_QUERY = 2
    }
}
