package com.abrarshakhi.smsman.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.domain.model.SearchHit
import com.abrarshakhi.smsman.ui.util.formatRelativeListTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInThreadScreen(
    threadId: Long,
    onBack: () -> Unit,
    onResultClick: (messageId: Long) -> Unit = {},
    viewModel: SearchInThreadViewModel = hiltViewModel(),
) {
    LaunchedEffect(threadId) { viewModel.bind(threadId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Search in conversation") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isSearching -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.query.isEmpty() -> SearchInThreadPrompt("Type to search in this conversation.")
                state.hits.isEmpty() && state.hasSearched -> SearchInThreadPrompt("No matches for \"${state.query}\".")
                state.hits.isEmpty() -> SearchInThreadPrompt("Keep typing…")
                else -> InThreadResults(
                    hits = state.hits,
                    query = state.query,
                    onClick = onResultClick,
                )
            }
        }
    }
}

@Composable
private fun SearchInThreadPrompt(text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InThreadResults(
    hits: List<SearchHit>,
    query: String,
    onClick: (Long) -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("d MMM, HH:mm") }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(hits, key = { it.message.id }) { hit ->
            InThreadResultRow(
                hit = hit,
                query = query,
                timeFormatter = timeFormatter,
                onClick = { onClick(hit.message.id) },
            )
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun InThreadResultRow(
    hit: SearchHit,
    query: String,
    timeFormatter: DateTimeFormatter,
    onClick: () -> Unit,
) {
    val timestamp = remember(hit.message.date) {
        Instant.ofEpochMilli(hit.message.date).atZone(ZoneId.systemDefault()).format(timeFormatter)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = buildHighlighted(hit.snippet, query),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildHighlighted(snippet: String, query: String): AnnotatedString {
    if (query.isEmpty() || snippet.isEmpty()) return AnnotatedString(snippet)
    return buildAnnotatedString {
        var cursor = 0
        val lower = snippet.lowercase()
        val needle = query.lowercase()
        while (cursor < snippet.length) {
            val match = lower.indexOf(needle, cursor)
            if (match < 0) { append(snippet.substring(cursor)); break }
            append(snippet.substring(cursor, match))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(snippet.substring(match, match + needle.length))
            }
            cursor = match + needle.length
        }
    }
}
