package com.abrarshakhi.smsman.ui.component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

@Composable
fun SwipeToGoBack(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    val gesture = remember { SwipeBackGesture() }
    val flingVelocity = with(LocalDensity.current) { FlingVelocity.toPx() }

    DisposableEffect(dispatcher, gesture) {
        dispatcher?.addInput(gesture.input)
        onDispose { dispatcher?.removeInput(gesture.input) }
    }

    Box(
        modifier = modifier
            .onSizeChanged { gesture.width = it.width.toFloat() }
            .draggable(
                state = rememberDraggableState(gesture::onDrag),
                orientation = Orientation.Horizontal,
                enabled = enabled && dispatcher != null,
                onDragStarted = { position -> gesture.onStart(position) },
                onDragStopped = { velocity -> gesture.onStop(velocity, flingVelocity) },
            ),
    ) {
        content()
    }
}

private class SwipeBackGesture {

    val input = DirectNavigationEventInput()
    var width = 0f

    private var phase = Phase.Idle
    private var origin = Offset.Zero
    private var distance = 0f

    fun onStart(position: Offset) {
        origin = position
        distance = 0f
        phase = Phase.Pending
    }

    fun onDrag(delta: Float) {
        when (phase) {
            Phase.Pending -> if (delta > 0f && width > 0f) {
                phase = Phase.Tracking
                input.backStarted(event())
                advance(delta)
            } else {
                phase = Phase.Rejected
            }

            Phase.Tracking -> advance(delta)
            Phase.Idle, Phase.Rejected -> Unit
        }
    }

    fun onStop(velocity: Float, flingVelocity: Float) {
        if (phase == Phase.Tracking) {
            if (distance / width >= COMPLETION_FRACTION || velocity >= flingVelocity) {
                input.backCompleted()
            } else {
                input.backCancelled()
            }
        }
        phase = Phase.Idle
    }

    private fun advance(delta: Float) {
        distance = (distance + delta).coerceIn(0f, width)
        input.backProgressed(event())
    }

    private fun event(): NavigationEvent = NavigationEvent(
        swipeEdge = NavigationEvent.EDGE_LEFT,
        progress = if (width > 0f) distance / width else 0f,
        touchX = origin.x + distance,
        touchY = origin.y,
    )

    private enum class Phase { Idle, Pending, Tracking, Rejected }
}

private const val COMPLETION_FRACTION = 0.35f
private val FlingVelocity = 600.dp
