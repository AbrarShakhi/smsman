package com.abrarshakhi.smsman.features.search.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.common.main.BackNavigationIcon
import com.abrarshakhi.smsman.common.main.ScreenChrome
import org.koin.compose.viewmodel.koinActivityViewModel

/**
 * The search field lives in the top bar, as it does in Messages. AppRoot renders the top bar
 * outside the NavEntry's ViewModel store, so the field and the results cannot share a
 * navigation-scoped ViewModel; an activity-scoped one is used instead so both observe the same
 * query.
 */
fun searchChrome() = ScreenChrome(
    title = "Search",
    topBar = { backStack, _ ->
        val viewModel: SearchViewModel = koinActivityViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        TopAppBar(
            navigationIcon = { BackNavigationIcon(backStack) },
            title = {
                TextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    placeholder = { Text("Search messages and people") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
            },
            actions = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            },
        )
    },
)
