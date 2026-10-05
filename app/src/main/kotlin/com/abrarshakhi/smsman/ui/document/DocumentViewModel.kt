package com.abrarshakhi.smsman.ui.document

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.smsman.data.document.DocumentRepository
import com.abrarshakhi.smsman.model.AppDocument
import com.abrarshakhi.smsman.model.MarkdownBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "DocumentViewModel"

data class DocumentState(
    val blocks: List<MarkdownBlock> = emptyList(),
    val isLoading: Boolean = true,
    val isUnavailable: Boolean = false,
)

class DocumentViewModel(
    repository: DocumentRepository,
    document: AppDocument,
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentState())
    val state: StateFlow<DocumentState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = try {
                DocumentState(blocks = repository.load(document).withoutTitle(), isLoading = false)
            } catch (e: IOException) {
                Log.e(TAG, "Could not open ${document.assetPath}", e)
                DocumentState(isLoading = false, isUnavailable = true)
            }
        }
    }
}

private fun List<MarkdownBlock>.withoutTitle(): List<MarkdownBlock> =
    if ((firstOrNull() as? MarkdownBlock.Heading)?.level == 1) drop(1) else this
