package com.abrarshakhi.smsman.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.common.ui.theme.fontFamilyFor
import com.abrarshakhi.smsman.common.util.isDynamicColorSchemeSupported
import com.abrarshakhi.smsman.core.settings.ColorSchemeOption
import com.abrarshakhi.smsman.core.settings.FontOption
import com.abrarshakhi.smsman.core.settings.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showFontPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
            ColorSchemeGrid(
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
private fun ColorSchemeGrid(
    selected: ColorSchemeOption,
    onSelect: (ColorSchemeOption) -> Unit,
) {
    val options = ColorSchemeOption.entries.toList()

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        options.chunked(6).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { option ->
                    if (!isDynamicColorSchemeSupported() && option == ColorSchemeOption.DYNAMIC) return
                    val isSelected = option == selected
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                option.seed?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
                            )
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape,
                            )
                            .clickable {
                                onSelect(option)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = option.label,
                                tint = Color.White,
                            )
                        } else if (option == ColorSchemeOption.DYNAMIC) {
                            Text(
                                text = "A",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = selected.label,
            modifier = Modifier.padding(
                top = 4.dp,
                bottom = 8.dp,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
