package com.abrarshakhi.smsman.ui.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.ui.screens.chat.ChatItem
import kotlinx.coroutines.launch

/**
 * Reverse-laid LazyColumn of message bubbles + day dividers. Newest message is at the bottom.
 * Jump-to-bottom FAB appears when the user is scrolled away from index 0.
 */
@Composable
fun Messages(
    items: List<ChatItem>,
    modifier: Modifier = Modifier,
    onMessageLongPress: (Long) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                count = items.size,
                key = { idx -> items[idx].key },
                contentType = { idx -> if (items[idx] is ChatItem.Msg) "msg" else "day" },
            ) { idx ->
                when (val item = items[idx]) {
                    is ChatItem.Msg -> MessageBubble(item, onLongPress = onMessageLongPress)
                    is ChatItem.DayDivider -> DayHeader(item.label)
                }
            }
        }

        val showJump by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > JUMP_THRESHOLD_PX
            }
        }
        AnimatedVisibility(
            visible = showJump,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            FloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Jump to latest")
            }
        }
    }
}

private const val JUMP_THRESHOLD_PX = 200
