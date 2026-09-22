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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.common.ui.theme.fontFamilyFor
import com.abrarshakhi.smsman.core.settings.ColorSchemeOption
import com.abrarshakhi.smsman.core.settings.FontOption
import com.abrarshakhi.smsman.core.settings.ThemeMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(modifier.fillMaxSize()) {
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

        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
        item { SectionHeader("Colour scheme") }
        item {
            ColorSchemeGrid(
                selected = settings.colorScheme,
                onSelect = viewModel::onColorScheme,
            )
        }

        item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
        item { SectionHeader("Font") }
        items(FontOption.entries.toList()) { option ->
            OptionRow(
                label = option.label,
                selected = settings.font == option,
                onSelect = { viewModel.onFont(option) },
                // Preview each family in its own face so the choice is legible before applying.
                labelFamily = fontFamilyFor(option),
            )
        }
        item {
            Text(
                text = "Fonts other than the system default are downloaded on first use and " +
                    "fall back to the system font if unavailable.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
private fun OptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    labelFamily: FontFamily? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.let {
                if (labelFamily != null) it.copy(fontFamily = labelFamily) else it
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
    Column(Modifier.padding(horizontal = 16.dp)) {
        options.chunked(6).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { option ->
                    val isSelected = option == selected
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                option.seed?.let { Color(it) }
                                    ?: MaterialTheme.colorScheme.primary,
                            )
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape,
                            )
                            .clickable { onSelect(option) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = option.label, tint = Color.White)
                        } else if (option == ColorSchemeOption.DYNAMIC) {
                            Text("A", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
        Text(
            text = selected.label,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
