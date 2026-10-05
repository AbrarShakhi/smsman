package com.abrarshakhi.smsman.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

object TopLevelMetadataKey : NavMetadataKey<Boolean>

fun topLevelMetadata(): Map<String, Any> = metadata { put(TopLevelMetadataKey, true) }

private val Scene<*>.isTopLevel: Boolean get() = metadata[TopLevelMetadataKey] == true

class NavTransitions(private val motion: MotionScheme) {

    fun <T : Any> forward(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        if (initialState.isTopLevel && targetState.isTopLevel) fadeThrough() else sharedAxisX(forward = true)
    }

    fun <T : Any> pop(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
        if (initialState.isTopLevel && targetState.isTopLevel) fadeThrough() else sharedAxisX(forward = false)
    }

    fun <T : Any> predictivePop():
        AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform = { edge ->
        val direction = if (edge == NavigationEvent.EDGE_RIGHT) -1 else 1
        val follow = tween<IntOffset>(PREDICTIVE_MILLIS, easing = LinearEasing)
        val reveal = tween<Float>(PREDICTIVE_MILLIS, easing = LinearEasing)
        ContentTransform(
            targetContentEnter = slideInHorizontally(follow) { -it / PARALLAX_FRACTION * direction } +
                fadeIn(reveal, initialAlpha = REVEAL_ALPHA) +
                scaleIn(reveal, initialScale = REVEAL_SCALE),
            initialContentExit = slideOutHorizontally(follow) { it * direction },
            targetContentZIndex = -1f,
        )
    }

    private fun fadeThrough(): ContentTransform =
        (fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.defaultSpatialSpec(), initialScale = PEEK_SCALE)) togetherWith
            fadeOut(motion.fastEffectsSpec())

    private fun sharedAxisX(forward: Boolean): ContentTransform {
        val direction = if (forward) 1 else -1
        val slide = motion.defaultSpatialSpec<IntOffset>()
        return (slideInHorizontally(slide) { it / SLIDE_FRACTION * direction } + fadeIn(motion.defaultEffectsSpec())) togetherWith
            (slideOutHorizontally(slide) { -it / SLIDE_FRACTION * direction } + fadeOut(motion.fastEffectsSpec()))
    }

    private companion object {
        const val SLIDE_FRACTION = 4
        const val PARALLAX_FRACTION = 5
        const val PREDICTIVE_MILLIS = 320
        const val PEEK_SCALE = 0.94f
        const val REVEAL_SCALE = 0.96f
        const val REVEAL_ALPHA = 0.6f
    }
}
