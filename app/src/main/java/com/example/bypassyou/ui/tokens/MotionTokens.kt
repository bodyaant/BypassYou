package com.example.bypassyou.ui.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * Material Design 3 Expressive Motion Tokens (m3.material.io/styles/motion).
 * Official duration, easing curves and spring physics specifications.
 */
object MotionTokens {

    // --- Durations in Milliseconds ---
    object Duration {
        const val Short1 = 50
        const val Short2 = 100
        const val Short3 = 150
        const val Short4 = 200

        const val Medium1 = 250
        const val Medium2 = 300
        const val Medium3 = 350
        const val Medium4 = 400

        const val Long1 = 450
        const val Long2 = 500
        const val Long3 = 550
        const val Long4 = 600

        const val ExtraLong1 = 700
        const val ExtraLong2 = 800
        const val ExtraLong3 = 900
        const val ExtraLong4 = 1000
    }

    // --- Official M3 Easing Curves ---
    object Easing {
        val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

        val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val StandardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
        val StandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

        val ExpressiveFastSpatial = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val ExpressiveSlowSpatial = CubicBezierEasing(0.3f, 0.0f, 0.0f, 1.0f)
    }

    // --- Material 3 Expressive Spring Physics ---
    object Springs {
        /**
         * Signature M3 Expressive bouncy spring.
         * Used for touch feedback, scale bounces, slider snapping, and organic morphs.
         */
        fun <T> expressiveBouncy(): SpringSpec<T> = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow
        )

        /**
         * Playful bouncy spring with visible overshoot for hero celebrations and high-energy interactions.
         */
        fun <T> effectsBouncy(): SpringSpec<T> = spring(
            dampingRatio = 0.55f,
            stiffness = 400f
        )

        /**
         * Fast bouncy spring for micro-interactions (e.g. checkbox check, toggle flick, badge pop).
         */
        fun <T> effectsFastBouncy(): SpringSpec<T> = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMedium
        )

        fun <T> expressiveFastBouncy(): SpringSpec<T> = effectsFastBouncy()

        /**
         * Spatial spring for large container expansions and screen transforms.
         */
        fun <T> spatialDefault(): SpringSpec<T> = spring(
            dampingRatio = 0.80f,
            stiffness = 380f
        )

        /**
         * Fast spatial spring for quick position tracking and bottom sheet snaps.
         */
        fun <T> spatialFast(): SpringSpec<T> = spring(
            dampingRatio = 0.80f,
            stiffness = Spring.StiffnessMedium
        )

        /**
         * Standard critically-damped spring without overshoot.
         */
        fun <T> standard(): SpringSpec<T> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        /**
         * Gentle, soft spring for expansive full-screen container shifts.
         */
        fun <T> gentle(): SpringSpec<T> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    }
}
