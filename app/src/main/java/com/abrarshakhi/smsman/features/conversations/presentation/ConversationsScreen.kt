package com.abrarshakhi.smsman.features.conversations.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Backs both the All messages and Favorite tabs; [favoritesOnly] selects the filter. */
@Composable
fun ConversationsScreen(favoritesOnly: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (favoritesOnly) "Favorite" else "All messages",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
