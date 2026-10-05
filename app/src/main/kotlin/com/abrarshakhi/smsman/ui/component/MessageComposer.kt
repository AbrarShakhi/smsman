package com.abrarshakhi.smsman.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.abrarshakhi.smsman.R
import com.abrarshakhi.smsman.model.SimInfo
import com.abrarshakhi.smsman.sms.SegmentInfo
import kotlinx.coroutines.launch

@Composable
fun MessageComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    canSend: Boolean,
    onSend: () -> Unit,
    segments: SegmentInfo,
    sims: List<SimInfo>,
    selectedSim: SimInfo?,
    onSimSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val motion = MaterialTheme.motionScheme
    val enter = expandVertically(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec())
    val exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        AnimatedVisibility(visible = error != null, enter = enter, exit = exit) {
            Text(
                text = error.orEmpty(),
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        AnimatedVisibility(visible = segments.segments > 1, enter = enter, exit = exit) {
            Text(
                text = segmentsLabel(segments),
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (sims.size > 1) {
                        SimSelector(
                            sims = sims,
                            selected = selectedSim,
                            onSelect = onSimSelected,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    TextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.composer_placeholder)) },
                        maxLines = MAX_LINES,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
            SendButton(enabled = canSend, onClick = onSend)
        }
    }
}

@Composable
private fun segmentsLabel(segments: SegmentInfo): String {
    val count = pluralStringResource(
        R.plurals.composer_segments,
        segments.segments,
        segments.segments,
        segments.remainingInSegment,
    )
    return if (segments.isUnicode) "$count · ${stringResource(R.string.composer_unicode)}" else count
}

@Composable
private fun SimSelector(
    sims: List<SimInfo>,
    selected: SimInfo?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val height = ButtonDefaults.ExtraSmallContainerHeight

    Box(modifier = modifier) {
        FilledTonalButton(
            onClick = { open = true },
            shapes = ButtonDefaults.shapesFor(height),
            modifier = Modifier.heightIn(min = height),
            contentPadding = ButtonDefaults.contentPaddingFor(height),
        ) {
            Text(
                text = selected?.let { stringResource(R.string.chat_sim, it.slotIndex + 1) }
                    ?: stringResource(R.string.composer_sim),
                style = ButtonDefaults.textStyleFor(height),
            )
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            shape = MaterialTheme.shapes.large,
        ) {
            sims.forEach { sim ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_sim_detail, sim.slotIndex + 1, sim.label)) },
                    onClick = {
                        onSelect(sim.subscriptionId)
                        open = false
                    },
                    trailingIcon = if (sim.subscriptionId == selected?.subscriptionId) {
                        { Icon(Icons.Rounded.Check, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val motion = MaterialTheme.motionScheme
    val departure = remember { Animatable(0f) }
    val arrival = remember { Animatable(1f) }

    FilledIconButton(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
            scope.launch {
                departure.animateTo(1f, tween(FLIGHT_MILLIS, easing = FastOutLinearInEasing))
                arrival.snapTo(0f)
                departure.snapTo(0f)
                arrival.animateTo(1f, motion.fastSpatialSpec())
            }
        },
        shapes = IconButtonDefaults.shapes(),
        enabled = enabled,
        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.Send,
            contentDescription = stringResource(R.string.composer_send),
            modifier = Modifier.graphicsLayer {
                val distance = FlightDistance.toPx()
                translationX = departure.value * distance
                translationY = -departure.value * distance
                rotationZ = -FLIGHT_ROTATION * departure.value
                scaleX = arrival.value
                scaleY = arrival.value
                alpha = (1f - departure.value) * arrival.value.coerceIn(0f, 1f)
            },
        )
    }
}

private const val MAX_LINES = 5
private const val FLIGHT_MILLIS = 260
private const val FLIGHT_ROTATION = 30f
private val FlightDistance = 32.dp
