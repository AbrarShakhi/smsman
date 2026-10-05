package com.abrarshakhi.smsman.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.ui.components.BackNavigationIcon
import com.abrarshakhi.smsman.common.ui.theme.fontFamilyFor
import com.abrarshakhi.smsman.common.util.isDynamicColorSchemeSupported
import com.abrarshakhi.smsman.core.settings.ColorSchemeOption
import com.abrarshakhi.smsman.core.settings.FontOption
import com.abrarshakhi.smsman.core.settings.ThemeMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(onBack: () -> Unit) {
    SettingsScreen(viewModel = koinViewModel(), onBack = onBack)
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showFontPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item { SectionHeader("Theme") }
            items(ThemeMode.entries.toList()) { mode ->
                OptionRow(
                    label = when (mode) {
                        ThemeMode.SYSTEM -> "Follow system"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    },
                    selected = settings.themeMode == mode,
                    onSelect = { viewModel.onThemeMode(mode) },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SectionHeader("Colour scheme") }

            item {
                ColorSchemeRow(
                    selected = settings.colorScheme, onSelect = viewModel::onColorScheme
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item { SectionHeader("Font") }

            item { FontSelector(selected = settings.font, onClick = { showFontPicker = true }) }

            item {
                Text(
                    text = stringResource(R.string.setting_font_description),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showFontPicker) {
        FontPickerDialog(
            selected = settings.font,
            onSelect = { font ->
                viewModel.onFont(font)
                showFontPicker = false
            },
            onDismiss = {
                showFontPicker = false
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 4.dp,
        ),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun FontSelector(
    selected: FontOption,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = null,
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Choose font",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        overlineContent = null,
        supportingContent = {
            Text(
                text = selected.label,
                fontFamily = fontFamilyFor(selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(),
        content = {
            Text(
                text = "Font",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
    )
}

@Composable
private fun FontPickerDialog(
    selected: FontOption,
    onSelect: (FontOption) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Font")
        },
        text = {
            LazyColumn {
                items(items = FontOption.entries.toList(), key = { it.name }) { option ->
                    FontOptionRow(
                        option = option,
                        selected = option == selected,
                        onSelect = { onSelect(option) },
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FontOptionRow(
    option: FontOption,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
            )
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
        )

        Text(
            text = option.label,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = fontFamilyFor(option),
            ),
        )
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    labelFamily: FontFamily? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
            )
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.let {
                if (labelFamily != null) {
                    it.copy(fontFamily = labelFamily)
                } else {
                    it
                }
            },
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun ColorSchemeRow(
    selected: ColorSchemeOption,
    onSelect: (ColorSchemeOption) -> Unit,
) {
    val dynamicBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFFBBC05), Color(0xFFEA4335)
            )
        )
    }

    LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(ColorSchemeOption.entries.toList()) { option ->
            if (!isDynamicColorSchemeSupported() && option == ColorSchemeOption.DYNAMIC) return@items

            Box(
                modifier = Modifier.clickable(onClick = { onSelect(option) }),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val baseModifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)

                    val modifier = if (option == ColorSchemeOption.DYNAMIC) {
                        baseModifier.background(dynamicBrush)
                    } else {
                        baseModifier.background(option.seed?.let { Color(it) }
                            ?: MaterialTheme.colorScheme.primary)
                    }

                    if (option == selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            modifier = modifier.padding(10.dp),
                            contentDescription = "check icon",
                            tint = Color.White
                        )
                    } else {
                        Box(modifier = modifier)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = option.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
