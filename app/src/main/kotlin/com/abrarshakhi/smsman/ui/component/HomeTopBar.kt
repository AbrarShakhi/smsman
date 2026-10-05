package com.abrarshakhi.smsman.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import com.abrarshakhi.smsman.R

@Composable
fun HomeTopBar(
    title: String,
    subtitle: String?,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    LargeFlexibleTopAppBar(
        title = { Text(text = title, fontStyle = FontStyle.Italic) },
        subtitle = {
            val motion = MaterialTheme.motionScheme
            AnimatedContent(
                targetState = subtitle.orEmpty(),
                transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            ) { text -> Text(text) }
        },
        modifier = modifier,
        actions = {
            IconButton(onClick = onOpenSearch, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.action_search))
            }
            OverflowMenu(onOpenSettings = onOpenSettings)
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun OverflowMenu(onOpenSettings: () -> Unit) {
    var open by remember { mutableStateOf(false) }

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
                text = { Text(stringResource(R.string.action_settings)) },
                leadingIcon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                onClick = {
                    open = false
                    onOpenSettings()
                },
            )
        }
    }
}
