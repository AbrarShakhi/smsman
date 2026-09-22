package com.abrarshakhi.smsman.features.search.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.LocalAppBackStack
import com.abrarshakhi.smsman.common.navigation.navigateTo
import com.abrarshakhi.smsman.common.ui.components.ContactAvatar
import com.abrarshakhi.smsman.common.util.formatConversationTime
import com.abrarshakhi.smsman.core.model.ContactSuggestion
import com.abrarshakhi.smsman.core.model.Conversation
import com.abrarshakhi.smsman.core.repository.MessageHit

@Composable
fun SearchScreen(viewModel: SearchViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = LocalAppBackStack.current
    val context = LocalContext.current

    when {
        state.query.isBlank() -> Centered(modifier, "Search messages and contacts")

        state.isSearching -> Box(modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        state.results.isEmpty -> Centered(modifier, "No results for \"${state.query}\"")

        else -> LazyColumn(modifier.fillMaxSize()) {
            if (state.results.people.isNotEmpty()) {
                item { SectionHeader("People") }
                items(state.results.people, key = { "person-${it.number}" }) { person ->
                    PersonRow(person) {
                        // Opening a person starts a conversation with them.
                        backStack.navigateTo(AppRouteKey.NewMessage)
                    }
                }
                item { HorizontalDivider() }
            }
            if (state.results.messages.isNotEmpty()) {
                item { SectionHeader("Messages") }
                items(state.results.messages, key = { "msg-${it.message.id}" }) { hit ->
                    MessageHitRow(hit, formatConversationTime(context, hit.message.date)) {
                        backStack.navigateTo(
                            AppRouteKey.Chat(
                                threadId = hit.message.threadId,
                                highlightMessageId = hit.message.id,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Centered(modifier: Modifier, text: String) {
    Box(modifier.fillMaxSize(), Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun PersonRow(person: ContactSuggestion, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactAvatar(person.label, person.number.hashCode(), size = 40)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(person.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = person.number,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageHitRow(hit: MessageHit, timestamp: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ContactAvatar(
            displayName = hit.senderLabel,
            colorIndex = (hit.message.threadId % Conversation.AVATAR_COLOR_SLOTS).toInt(),
            size = 40,
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = hit.senderLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(2.dp))
            Text(
                text = hit.message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
