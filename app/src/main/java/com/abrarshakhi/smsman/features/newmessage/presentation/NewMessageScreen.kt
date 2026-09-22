package com.abrarshakhi.smsman.features.newmessage.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.common.navigation.AppRouteKey
import com.abrarshakhi.smsman.common.navigation.LocalAppBackStack
import com.abrarshakhi.smsman.common.navigation.back
import com.abrarshakhi.smsman.common.navigation.navigateTo
import com.abrarshakhi.smsman.common.ui.components.ContactAvatar

@Composable
fun NewMessageScreen(viewModel: NewMessageViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStack = LocalAppBackStack.current
    var simMenuOpen by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.recipient,
            onValueChange = viewModel::onRecipientChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            label = { Text("To") },
            placeholder = { Text("Name or phone number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        HorizontalDivider()

        Box(Modifier.weight(1f)) {
            if (state.suggestions.isNotEmpty()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.suggestions, key = { it.number }) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onSuggestionSelected(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ContactAvatar(
                                displayName = suggestion.label,
                                colorIndex = suggestion.number.hashCode(),
                                size = 40,
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(suggestion.label, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = suggestion.number,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        text = if (state.recipient.isBlank()) {
                            "Enter a name or number to start"
                        } else {
                            "No matching contacts — the number will be used as typed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        state.error?.let { error ->
            Text(
                text = error,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth().imePadding(),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (state.segments.segments > 1) {
                    Text(
                        text = "${state.segments.segments} messages · " +
                            "${state.segments.remainingInSegment} left" +
                            if (state.segments.isUnicode) " · Unicode" else "",
                        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isMultiSim) {
                        Box {
                            AssistChip(
                                onClick = { simMenuOpen = true },
                                label = {
                                    Text(state.selectedSim?.let { "SIM ${it.slotIndex + 1}" } ?: "SIM")
                                },
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                            DropdownMenu(simMenuOpen, { simMenuOpen = false }) {
                                state.sims.forEach { sim ->
                                    DropdownMenuItem(
                                        text = { Text("SIM ${sim.slotIndex + 1} · ${sim.label}") },
                                        onClick = {
                                            viewModel.onSimSelected(sim.subscriptionId)
                                            simMenuOpen = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.draft,
                        onValueChange = viewModel::onDraftChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Text message") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 5,
                    )
                    FilledIconButton(
                        onClick = {
                            viewModel.onSend { threadId ->
                                // Replace this screen with the thread it created.
                                backStack.back()
                                backStack.navigateTo(AppRouteKey.Chat(threadId))
                            }
                        },
                        enabled = state.canSend,
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
