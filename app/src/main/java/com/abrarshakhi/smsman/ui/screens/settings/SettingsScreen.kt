package com.abrarshakhi.smsman.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.smsman.domain.model.Sim
import com.abrarshakhi.smsman.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onBlockedNumbers: () -> Unit,
    onBackup: () -> Unit,
    onScheduled: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val defaultAppLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refreshDefaultAppState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.RequestDefaultSmsApp -> defaultAppLauncher.launch(effect.intent)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!state.isDefaultSmsApp) {
                item { DefaultAppBanner(onSet = viewModel::requestDefaultApp) }
            }

            item { SectionHeader("Appearance") }
            item {
                ThemePickerRow(
                    current = state.settings.themeMode,
                    onSelect = viewModel::setThemeMode,
                )
            }
            item {
                SwitchRow(
                    title = "Material You dynamic color",
                    subtitle = "Use the system color palette (Android 12+).",
                    checked = state.settings.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

            item { SectionHeader("Receipts") }
            item {
                SwitchRow(
                    title = "Request delivery reports",
                    subtitle = "Carrier-dependent. Shown under sent messages.",
                    checked = state.settings.requestDeliveryReports,
                    onCheckedChange = viewModel::setRequestDeliveryReports,
                )
            }
            item {
                SwitchRow(
                    title = "Request MMS read receipts",
                    subtitle = "Only applies to MMS. Off by default for privacy.",
                    checked = state.settings.requestReadReceipts,
                    onCheckedChange = viewModel::setRequestReadReceipts,
                )
            }

            if (state.sims.size > 1) {
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                item { SectionHeader("SIM cards") }
                item {
                    SimPickerRow(
                        sims = state.sims,
                        selectedId = state.settings.defaultSubscriptionId,
                        onSelect = viewModel::setDefaultSim,
                    )
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

            item { SectionHeader("More") }
            item {
                NavRow(
                    icon = Icons.Filled.Block,
                    title = "Blocked numbers",
                    subtitle = "Manage blocked senders",
                    onClick = onBlockedNumbers,
                )
            }
            item {
                NavRow(
                    icon = Icons.Filled.Backup,
                    title = "Backup & restore",
                    subtitle = "Export to XML / import (M5f)",
                    onClick = onBackup,
                )
            }
            item {
                NavRow(
                    icon = Icons.Filled.Schedule,
                    title = "Scheduled messages",
                    subtitle = "Pending scheduled sends (M5c)",
                    onClick = onScheduled,
                )
            }
        }
    }
}

@Composable
private fun DefaultAppBanner(onSet: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Not your default SMS app",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = "SMS Man can only send and receive messages when it is the default. Tap below to switch.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.size(8.dp))
            AssistChip(
                onClick = onSet,
                label = { Text("Set as default") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    labelColor = MaterialTheme.colorScheme.onError,
                ),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePickerRow(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = mode == current,
                    onClick = { onSelect(mode) },
                    label = { Text(mode.label) },
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onCheckedChange(!checked) },
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun NavRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun SimPickerRow(
    sims: List<Sim>,
    selectedId: Int?,
    onSelect: (Int?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Default SIM for sending",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text("Ask each time") },
            )
            sims.forEach { sim ->
                FilterChip(
                    selected = sim.subscriptionId == selectedId,
                    onClick = { onSelect(sim.subscriptionId) },
                    label = { Text(sim.displayName) },
                    leadingIcon = { Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                )
            }
        }
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "System"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
        ThemeMode.Black -> "AMOLED"
    }
