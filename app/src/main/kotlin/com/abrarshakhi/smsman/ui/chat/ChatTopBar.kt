package com.abrarshakhi.smsman.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.ThreadTitle
import com.abrarshakhi.smsman.ui.component.BackNavigationIcon
import com.abrarshakhi.smsman.ui.component.ContactAvatar

@Composable
internal fun ConversationTopBar(
    title: ThreadTitle?,
    avatarColorIndex: Int,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMarkUnread: () -> Unit,
    onDeleteConversation: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = title != null,
                enter = fadeIn(motion.defaultEffectsSpec()) +
                    slideInHorizontally(motion.defaultSpatialSpec()) { width -> width / TITLE_SLIDE_DIVISOR },
            ) {
                if (title != null) ConversationTitle(title = title, avatarColorIndex = avatarColorIndex)
            }
        },
        modifier = modifier,
        navigationIcon = { BackNavigationIcon(onClick = onBack) },
        actions = {
            FavoriteToggle(isFavorite = isFavorite, onToggle = onToggleFavorite)
            ConversationMenu(onMarkUnread = onMarkUnread, onDeleteConversation = onDeleteConversation)
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ConversationTitle(title: ThreadTitle, avatarColorIndex: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ContactAvatar(displayName = title.title, colorIndex = avatarColorIndex, size = 40.dp)
        Column {
            Text(
                text = title.title,
                style = MaterialTheme.typography.titleMediumEmphasized,
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
}

@Composable
internal fun SelectionTopBar(
    count: Int,
    pinAction: Boolean,
    onClearSelection: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    TopAppBar(
        title = { SelectionCount(count = count) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onClearSelection, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.chat_clear_selection))
            }
        },
        actions = {
            IconToggleButton(
                checked = !pinAction,
                onCheckedChange = { onTogglePin() },
                shapes = IconButtonDefaults.toggleableShapes(),
                colors = IconButtonDefaults.iconToggleButtonColors(),
            ) {
                Icon(
                    imageVector = if (pinAction) Icons.Outlined.PushPin else Icons.Rounded.PushPin,
                    contentDescription = stringResource(if (pinAction) R.string.chat_pin else R.string.chat_unpin),
                )
            }
            IconButton(onClick = { confirmDelete = true }, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.secondaryContainer,
            scrolledContainerColor = colors.secondaryContainer,
            navigationIconContentColor = colors.onSecondaryContainer,
            titleContentColor = colors.onSecondaryContainer,
            actionIconContentColor = colors.onSecondaryContainer,
        ),
    )

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = pluralStringResource(R.plurals.chat_delete_messages_title, count, count),
            body = stringResource(R.string.chat_delete_messages_body),
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun SelectionCount(count: Int) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInVertically(motion.fastSpatialSpec()) { height -> height * direction } + fadeIn(motion.fastEffectsSpec()))
                .togetherWith(
                    slideOutVertically(motion.fastSpatialSpec()) { height -> -height * direction } +
                        fadeOut(motion.fastEffectsSpec()),
                )
                .using(SizeTransform(clip = false))
        },
    ) { value ->
        Text(
            text = pluralStringResource(R.plurals.chat_selected_count, value, value),
            style = MaterialTheme.typography.titleLargeEmphasized,
        )
    }
}

@Composable
private fun FavoriteToggle(isFavorite: Boolean, onToggle: () -> Unit) {
    val motion = MaterialTheme.motionScheme
    IconToggleButton(
        checked = isFavorite,
        onCheckedChange = { onToggle() },
        shapes = IconButtonDefaults.toggleableShapes(),
        colors = IconButtonDefaults.iconToggleButtonColors(),
    ) {
        AnimatedContent(
            targetState = isFavorite,
            transitionSpec = {
                val pop = spring<Float>(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
                (scaleIn(pop, initialScale = HEART_START_SCALE) + fadeIn(motion.fastEffectsSpec()))
                    .togetherWith(scaleOut(motion.fastSpatialSpec(), targetScale = HEART_START_SCALE) + fadeOut(motion.fastEffectsSpec()))
            },
        ) { favorite ->
            Icon(
                imageVector = if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(
                    if (favorite) R.string.chat_remove_favorite else R.string.chat_add_favorite,
                ),
            )
        }
    }
}

@Composable
private fun ConversationMenu(
    onMarkUnread: () -> Unit,
    onDeleteConversation: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Box {
        IconButton(onClick = { open = true }, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.action_more_options))
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            shape = MaterialTheme.shapes.large,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.chat_mark_unread)) },
                onClick = {
                    open = false
                    onMarkUnread()
                },
                leadingIcon = { Icon(Icons.Rounded.MarkChatUnread, contentDescription = null) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.chat_delete_conversation)) },
                onClick = {
                    open = false
                    confirmDelete = true
                },
                leadingIcon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null) },
            )
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.chat_delete_conversation_title),
            body = stringResource(R.string.chat_delete_conversation_body),
            onConfirm = {
                confirmDelete = false
                onDeleteConversation()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null) },
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private const val HEART_START_SCALE = 0.3f
private const val TITLE_SLIDE_DIVISOR = 8
