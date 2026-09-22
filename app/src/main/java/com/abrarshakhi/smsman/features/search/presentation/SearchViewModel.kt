package com.abrarshakhi.smsman.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.core.repository.SearchRepository
import com.abrarshakhi.smsman.core.repository.SearchResults
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val isSearching: Boolean = false,
)

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.value = _state.value.copy(query = query)

        // Each keystroke supersedes the previous search rather than racing it.
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(results = SearchResults(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            _state.value = _state.value.copy(isSearching = true)
            val results = repository.search(query)
            _state.value = _state.value.copy(results = results, isSearching = false)
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 250L
    }
}
