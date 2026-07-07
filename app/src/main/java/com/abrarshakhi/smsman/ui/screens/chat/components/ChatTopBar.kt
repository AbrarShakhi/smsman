package com.abrarshakhi.smsman.ui.screens.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.domain.model.Contact
import com.abrarshakhi.smsman.domain.model.Conversation
import com.abrarshakhi.smsman.ui.components.ContactAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    contact: Contact?,
    conversation: Conversation?,
    onBack: () -> Unit,
    onSearchInThread: () -> Unit,
    onCall: () -> Unit,
    onOverflow: () -> Unit,
) {
    val fallback = conversation?.recipientAddresses?.firstOrNull().orEmpty()
    val title = contact?.displayLabel ?: fallback.ifEmpty { "Conversation" }
    val subtitle = conversation?.recipientAddresses?.let { addrs ->
        when {
            addrs.size <= 1 -> null                    // 1-on-1: title says it all
            addrs.size == 2 -> "${addrs.size} participants"
            else -> "${addrs.size} participants"
        }
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ContactAvatar(contact = contact, fallbackLabel = fallback, size = 36.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Phone, contentDescription = "Call")
            }
            IconButton(onClick = onSearchInThread) {
                Icon(Icons.Default.Search, contentDescription = "Search in this conversation")
            }
            IconButton(onClick = onOverflow) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
