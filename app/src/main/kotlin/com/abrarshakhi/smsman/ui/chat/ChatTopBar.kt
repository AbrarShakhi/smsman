package com.abrarshakhi.smsman.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.model.ThreadTitle
import com.abrarshakhi.smsman.ui.component.BackNavigationIcon
import com.abrarshakhi.smsman.ui.component.ContactAvatar

@Composable
internal fun ChatTopBar(
    title: ThreadTitle,
    avatarColorIndex: Int,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkUnread: () -> Unit,
    onDeleteConversation: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ContactAvatar(
                    displayName = title.title,
                    colorIndex = avatarColorIndex,
                    size = 32,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    title.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        modifier = modifier,
        navigationIcon = { BackNavigationIcon(onClick = onBack) },
        actions = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove favourite" else "Add favourite",
                )
            }
            ChatOverflowMenu(
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onMarkUnread = onMarkUnread,
                onDeleteConversation = onDeleteConversation,
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ChatOverflowMenu(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onMarkUnread: () -> Unit,
    onDeleteConversation: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    IconButton(onClick = { open = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text(if (isFavorite) "Remove from favourites" else "Add to favourites") },
            onClick = {
                open = false
                onToggleFavorite()
            },
        )
        DropdownMenuItem(
            text = { Text("Mark as unread") },
            onClick = {
                open = false
                onMarkUnread()
            },
        )
        DropdownMenuItem(
            text = { Text("Delete conversation") },
            onClick = {
                open = false
                confirmDelete = true
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete conversation?") },
            text = {
                Text("Every message in this conversation is permanently removed from this device.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDeleteConversation()
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}
