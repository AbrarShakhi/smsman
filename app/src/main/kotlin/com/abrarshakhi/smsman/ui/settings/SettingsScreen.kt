package com.abrarshakhi.smsman.ui.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.AppDocument
import com.abrarshakhi.smsman.model.AppSettings
import com.abrarshakhi.smsman.model.ColorSchemeOption
import com.abrarshakhi.smsman.model.FontOption
import com.abrarshakhi.smsman.model.ThemeMode
import com.abrarshakhi.smsman.model.isDynamicColorSchemeSupported
import com.abrarshakhi.smsman.ui.component.BackNavigationIcon
import com.abrarshakhi.smsman.ui.component.ConnectedChoiceGroup
import com.abrarshakhi.smsman.ui.component.ListGroup
import com.abrarshakhi.smsman.ui.component.ListGroupItem
import com.abrarshakhi.smsman.ui.component.MorphShape
import com.abrarshakhi.smsman.ui.theme.fontFamilyFor
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
) {
    SettingsScreen(viewModel = koinViewModel(), onBack = onBack, onOpenDocument = onOpenDocument)
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenDocument: (AppDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showFontPicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "appearance") {
                AppearanceGroup(
                    settings = settings,
                    onThemeMode = viewModel::onThemeMode,
                    onColorScheme = viewModel::onColorScheme,
                    onOpenFontPicker = { showFontPicker = true },
                )
            }
            item(key = "about") { AboutGroup(onOpenDocument = onOpenDocument) }
            item(key = "open_source") { OpenSourceGroup() }
            item(key = "legal") { LegalGroup(onOpenDocument = onOpenDocument) }
        }
    }

    if (showFontPicker) {
        FontPickerDialog(
            selected = settings.font,
            onSelect = { font ->
                viewModel.onFont(font)
                showFontPicker = false
            },
            onDismiss = { showFontPicker = false },
        )
    }
}

@Composable
private fun AppearanceGroup(
    settings: AppSettings,
    onThemeMode: (ThemeMode) -> Unit,
    onColorScheme: (ColorSchemeOption) -> Unit,
    onOpenFontPicker: () -> Unit,
) {
    ListGroup(title = stringResource(R.string.settings_appearance)) {
        ListGroupItem {
            SettingTitle(title = stringResource(R.string.settings_theme))
            ConnectedChoiceGroup(
                options = ThemeMode.entries,
                selected = settings.themeMode,
                onSelect = onThemeMode,
                label = { themeModeLabel(it) },
            )
        }
        ListGroupItem {
            SettingTitle(
                title = stringResource(R.string.settings_color),
                supporting = if (settings.colorScheme == ColorSchemeOption.DYNAMIC) {
                    stringResource(R.string.settings_color_dynamic)
                } else {
                    stringResource(R.string.settings_color_accent, settings.colorScheme.label)
                },
            )
            ColorPicker(selected = settings.colorScheme, onSelect = onColorScheme)
        }
        ListGroupItem(onClick = onOpenFontPicker) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    SettingTitle(
                        title = stringResource(R.string.settings_font),
                        supporting = settings.font.label,
                        supportingFontFamily = fontFamilyFor(settings.font),
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.setting_font_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun SettingTitle(
    title: String,
    supporting: String? = null,
    supportingFontFamily: FontFamily? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (supporting != null) {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = supportingFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorPicker(selected: ColorSchemeOption, onSelect: (ColorSchemeOption) -> Unit) {
    val context = LocalContext.current
    val options = remember {
        ColorSchemeOption.entries.filter { it != ColorSchemeOption.DYNAMIC || isDynamicColorSchemeSupported() }
    }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        options.forEach { option ->
            ColorSwatch(
                color = swatchColor(context, option),
                label = option.label,
                selected = option == selected,
                showWallpaperIcon = option == ColorSchemeOption.DYNAMIC,
                onClick = { onSelect(option) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    showWallpaperIcon: Boolean,
    onClick: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val progress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = motion.defaultSpatialSpec(),
    )
    val rotation by animateFloatAsState(
        targetValue = if (selected) SELECTED_ROTATION else 0f,
        animationSpec = motion.slowSpatialSpec(),
    )
    val contentColor = if (color.luminance() > LIGHT_LUMINANCE) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(SwatchSize)
            .graphicsLayer { rotationZ = rotation }
            .clip(MorphShape(morph, progress.coerceIn(0f, 1f)))
            .background(color)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.graphicsLayer { rotationZ = -rotation }) {
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
                exit = scaleOut(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = contentColor)
            }
            if (showWallpaperIcon && !selected) {
                Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = contentColor)
            }
        }
    }
}

@Composable
private fun FontPickerDialog(
    selected: FontOption,
    onSelect: (FontOption) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_font)) },
        text = {
            LazyColumn {
                items(items = FontOption.entries, key = { it.name }) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .selectable(
                                selected = option == selected,
                                onClick = { onSelect(option) },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = null)
                        Text(
                            text = option.label,
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = fontFamilyFor(option),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

private fun swatchColor(context: Context, option: ColorSchemeOption): Color {
    if (option == ColorSchemeOption.DYNAMIC && isDynamicColorSchemeSupported()) {
        return Color(context.getColor(android.R.color.system_accent1_500))
    }
    return option.seed?.let(::Color) ?: Color.Unspecified
}

private const val SELECTED_ROTATION = 90f
private const val LIGHT_LUMINANCE = 0.5f
private val SwatchSize = 52.dp
