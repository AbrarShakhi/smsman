package com.abrarshakhi.smsman.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.domain.model.SearchHit
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

data class SearchInThreadState(
    val threadId: Long = -1L,
    val query: String = "",
    val hits: List<SearchHit> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchInThreadViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchInThreadState())
    val state: StateFlow<SearchInThreadState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { query ->
                    val threadId = _state.value.threadId
                    if (query.length < MIN_QUERY || threadId < 0) {
                        _state.update { it.copy(hits = emptyList(), isSearching = false, hasSearched = false) }
                        return@collect
                    }
                    _state.update { it.copy(isSearching = true) }
                    val hits = searchRepository.searchInThread(threadId, query)
                    _state.update { it.copy(hits = hits, isSearching = false, hasSearched = true) }
                }
        }
    }

    fun bind(threadId: Long) {
        if (_state.value.threadId == threadId) return
        _state.update { it.copy(threadId = threadId, query = "", hits = emptyList(), hasSearched = false) }
        queryFlow.value = ""
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
