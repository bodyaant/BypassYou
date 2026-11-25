package com.example.bypassyou.ui.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Shape Tokens (m3.material.io/styles/shape).
 * Official corner radius values and shapes.
 */
object ShapeTokens {
    /** 0dp - Rectangular / square edges */
    val CornerNone: Dp = 0.dp

    /** 4dp - Snackbars, small tooltips */
    val CornerExtraSmall: Dp = 4.dp

    /** 8dp - Chips, text field containers */
    val CornerSmall: Dp = 8.dp

    /** 12dp - Standard cards, small dialogs, menus */
    val CornerMedium: Dp = 12.dp

    /** 16dp - Large cards, bottom/side navigation drawers */
    val CornerLarge: Dp = 16.dp

    /** 20dp - M3 Expressive expanded component corners */
    val CornerLargeIncreased: Dp = 20.dp

    /** 28dp - Expressive Hero cards, Floating Action Buttons (FAB), dialogs, System Settings cards */
    val CornerExtraLarge: Dp = 28.dp

    /** 32dp - M3 Expressive prominent container surfaces */
    val CornerExtraLargeIncreased: Dp = 32.dp

    /** 48dp - M3 Expressive high-roundedness structural containers */
    val CornerExtraExtraLarge: Dp = 48.dp

    /** Fully rounded pill or circular shape (9999dp) */
    val CornerFull: Dp = 9999.dp

    // Predefined RoundedCornerShapes
    val None = RoundedCornerShape(CornerNone)
    val ExtraSmall = RoundedCornerShape(CornerExtraSmall)
    val Small = RoundedCornerShape(CornerSmall)
    val Medium = RoundedCornerShape(CornerMedium)
    val Large = RoundedCornerShape(CornerLarge)
    val LargeIncreased = RoundedCornerShape(CornerLargeIncreased)
    val ExtraLarge = RoundedCornerShape(CornerExtraLarge)
    val ExtraLargeIncreased = RoundedCornerShape(CornerExtraLargeIncreased)
    val ExtraExtraLarge = RoundedCornerShape(CornerExtraExtraLarge)
    val Full = CircleShape
}

/**
 * Item position within a clustered or grouped settings list.
 */
enum class GroupPosition {
    Single,
    Top,
    Middle,
    Bottom
}

/**
 * Generates an authentic System Settings clustered shape based on position in group.
 */
fun clusteredShape(
    position: GroupPosition,
    outerRadius: Dp = ShapeTokens.CornerLarge, // 16.dp canonical AOSP & InstallerX outer radius
    innerRadius: Dp = 5.dp // 5.dp canonical AOSP & InstallerX inner connection radius
): RoundedCornerShape {
    return when (position) {
        GroupPosition.Single -> RoundedCornerShape(outerRadius)
        GroupPosition.Top -> RoundedCornerShape(
            topStart = outerRadius,
            topEnd = outerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        GroupPosition.Middle -> RoundedCornerShape(innerRadius)
        GroupPosition.Bottom -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = outerRadius,
            bottomEnd = outerRadius
        )
    }
}

/**
 * Smoothly interpolates from one [CornerBasedShape] to another [CornerBasedShape] based on [fraction] (0f..1f).
 */
data class MorphCornerShape(
    val from: CornerBasedShape,
    val to: CornerBasedShape,
    val fraction: Float,
) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val safeFraction = fraction.coerceIn(0f, 1f)
        fun corner(start: androidx.compose.foundation.shape.CornerSize, stop: androidx.compose.foundation.shape.CornerSize) =
            androidx.compose.foundation.shape.CornerSize(
                androidx.compose.ui.util.lerp(start.toPx(size, density), stop.toPx(size, density), safeFraction)
            )
        return RoundedCornerShape(
            topStart = corner(from.topStart, to.topStart),
            topEnd = corner(from.topEnd, to.topEnd),
            bottomEnd = corner(from.bottomEnd, to.bottomEnd),
            bottomStart = corner(from.bottomStart, to.bottomStart),
        ).createOutline(size, layoutDirection, density)
    }
}
