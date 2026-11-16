// SPDX-License-Identifier: Apache-2.0
// Adapted from InstallerX-Revived (GPL-3.0) and AOSP System Settings specs
package com.example.bypassyou.ui.components

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.bypassyou.ui.theme.cardContainerColor
import com.example.bypassyou.ui.tokens.ShapeTokens
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens
import kotlin.math.expm1
import kotlin.math.roundToInt

// Authentic corner radius and gap specs directly from InstallerX-Revived and AOSP System Settings
val CanonicalOuterCornerRadius: Dp = 16.dp
val CanonicalConnectionRadius: Dp = 5.dp
val CanonicalSegmentedGap: Dp = 2.dp

private const val bouncyStiffness = 800f
private const val bouncyDamping = 0.5f

/**
 * CompositionLocal providing the dynamically computed [Shape] for items inside a [CanonicalSegmentedColumn].
 */
val LocalSegmentedItemShape = compositionLocalOf<Shape> { RoundedCornerShape(CanonicalOuterCornerRadius) }

/**
 * Section Header following native Android 15/16/17 (AOSP) and InstallerX-Revived hierarchy.
 * Uses Title/Sentence Case (never uppercase) with exact spatial rhythm (24dp top, 8dp bottom, 16dp start).
 */
@Composable
fun CanonicalSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .semantics { heading() }
            .padding(
                start = SystemLayoutTokens.ScreenHorizontalPadding,
                end = SystemLayoutTokens.ScreenHorizontalPadding,
                top = SystemLayoutTokens.SectionHeaderPaddingTop,
                bottom = SystemLayoutTokens.SectionHeaderPaddingBottom
            )
    )
}

/**
 * Android 17 System Section Header alias for [CanonicalSectionHeader].
 */
@Composable
fun SystemSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) = CanonicalSectionHeader(title = title, modifier = modifier, color = color)

/**
 * Item descriptor within [CanonicalSegmentedColumn].
 */
data class CanonicalSegmentedItemData(
    val key: Any?,
    val visible: Boolean,
    val customTopPadding: Dp? = null,
    val forceFlatTop: Boolean = false,
    val forceFlatBottom: Boolean = false,
    val content: @Composable (Shape) -> Unit,
)

/**
 * DSL scope to declare items inside a [CanonicalSegmentedColumn].
 */
class CanonicalSegmentedColumnScope {
    val items = mutableListOf<CanonicalSegmentedItemData>()

    /**
     * Adds an item with automatic shape calculation.
     */
    fun item(
        key: Any? = null,
        animatedVisibility: Boolean = true,
        topPadding: Dp? = null,
        forceFlatTop: Boolean = false,
        forceFlatBottom: Boolean = false,
        content: @Composable (Shape) -> Unit,
    ) {
        items.add(
            CanonicalSegmentedItemData(
                key = key ?: items.size,
                visible = animatedVisibility,
                customTopPadding = topPadding,
                forceFlatTop = forceFlatTop,
                forceFlatBottom = forceFlatBottom,
                content = content
            )
        )
    }

    /**
     * Adds an expandable pairing (header + conditionally visible body).
     * Smoothly flattens the connection seam between them with spring physics.
     */
    fun expandableItem(
        animatedVisibility: Boolean = true,
        expanded: Boolean,
        topPadding: Dp? = null,
        bottomPadding: Dp = 1.dp,
        topContent: @Composable (Shape) -> Unit,
        bottomContent: @Composable (Shape) -> Unit,
    ) {
        item(
            animatedVisibility = animatedVisibility,
            topPadding = topPadding,
            forceFlatBottom = expanded,
            content = topContent,
        )
        item(
            animatedVisibility = animatedVisibility && expanded,
            topPadding = bottomPadding,
            forceFlatTop = true,
            content = bottomContent,
        )
    }

    /**
     * Adds a canonical preference tile item.
     */
    fun preferenceItem(
        title: String,
        key: Any? = null,
        subtitle: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconTint: Color? = null,
        containerColor: Color? = null,
        trailingContent: (@Composable () -> Unit)? = null,
        showChevron: Boolean = false,
        enabled: Boolean = true,
        onClick: (() -> Unit)? = null,
    ) {
        item(key = key) { shape ->
            CanonicalPreferenceTile(
                title = title,
                subtitle = subtitle,
                leadingIcon = leadingIcon,
                leadingIconTint = leadingIconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = containerColor ?: MaterialTheme.cardContainerColor,
                shape = shape,
                trailingContent = trailingContent,
                showChevron = showChevron,
                enabled = enabled,
                onClick = onClick,
            )
        }
    }

    /**
     * Adds a canonical switch preference tile item.
     */
    fun switchItem(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        key: Any? = null,
        subtitle: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconTint: Color? = null,
        containerColor: Color? = null,
        enabled: Boolean = true,
    ) {
        val itemKey = key ?: title
        item(key = itemKey) { shape ->
            CanonicalSwitchPreferenceTile(
                title = title,
                checked = checked,
                onCheckedChange = onCheckedChange,
                subtitle = subtitle,
                leadingIcon = leadingIcon,
                leadingIconTint = leadingIconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = containerColor ?: MaterialTheme.cardContainerColor,
                shape = shape,
                enabled = enabled,
            )
        }
    }

    /**
     * Adds a canonical two-target preference tile item (left clickable title, right independent switch).
     */
    fun twoTargetSwitchItem(
        title: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        onTitleClick: () -> Unit,
        key: Any? = null,
        subtitle: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconTint: Color? = null,
        containerColor: Color? = null,
        enabled: Boolean = true,
    ) {
        val itemKey = key ?: title
        item(key = itemKey) { shape ->
            CanonicalTwoTargetPreferenceTile(
                title = title,
                checked = checked,
                onCheckedChange = onCheckedChange,
                onTitleClick = onTitleClick,
                subtitle = subtitle,
                leadingIcon = leadingIcon,
                leadingIconTint = leadingIconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = containerColor ?: MaterialTheme.cardContainerColor,
                shape = shape,
                enabled = enabled,
            )
        }
    }

    /**
     * Adds an arbitrary composable wrapped in a canonical surface matching the dynamic spring shape.
     */
    fun cardItem(
        key: Any? = null,
        containerColor: Color? = null,
        content: @Composable (Shape) -> Unit,
    ) {
        item(key = key) { shape ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                color = containerColor ?: MaterialTheme.cardContainerColor,
                tonalElevation = 0.dp
            ) {
                content(shape)
            }
        }
    }
}

/**
 * Authentic [CanonicalSegmentedColumn] ported from InstallerX-Revived and AOSP System Settings.
 *
 * Each item in this group is an independent Surface with:
 * - Top item: top corners [outerCornerRadius] (16dp), bottom corners [connectionRadius] (5dp).
 * - Middle items: top and bottom corners [connectionRadius] (5dp).
 * - Bottom item: top corners [connectionRadius] (5dp), bottom corners [outerCornerRadius] (16dp).
 * - Single item: all corners [outerCornerRadius] (16dp).
 * - Between items: a tiny [gap] (2dp) revealing the background beneath, forming that signature indented hairline seam!
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CanonicalSegmentedColumn(
    modifier: Modifier = Modifier,
    title: String = "",
    badgeText: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    outerCornerRadius: Dp = CanonicalOuterCornerRadius,
    connectionRadius: Dp = CanonicalConnectionRadius,
    gap: Dp = CanonicalSegmentedGap,
    contentPadding: PaddingValues = PaddingValues(horizontal = SystemLayoutTokens.ScreenHorizontalPadding),
    // Plain (non-@Composable) builder: it re-runs on every recomposition of this column, so
    // the item list always reflects current state. A @Composable builder would be restarted
    // in isolation and append to a stale scope the Layout never re-reads.
    content: CanonicalSegmentedColumnScope.() -> Unit,
) {
    val scope = CanonicalSegmentedColumnScope().apply { content() }
    val allItems = scope.items

    if (allItems.isEmpty()) return

    Column(modifier = modifier.padding(contentPadding)) {
        if (title.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() }
                    .padding(
                        start = 0.dp,
                        top = SystemLayoutTokens.SectionHeaderPaddingTop,
                        bottom = SystemLayoutTokens.SectionHeaderPaddingBottom
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor,
                )
                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        val floatSpring = spring<Float>(dampingRatio = bouncyDamping, stiffness = bouncyStiffness)
        val dpSpring = spring<Dp>(dampingRatio = bouncyDamping, stiffness = bouncyStiffness)

        val progresses = allItems.mapIndexed { index, item ->
            key(item.key ?: index) {
                animateFloatAsState(
                    targetValue = if (item.visible) 1f else 0f,
                    animationSpec = floatSpring,
                    label = "progress",
                )
            }
        }

        val firstVisibleIndex = allItems.indexOfFirst { it.visible }
        val lastVisibleIndex = allItems.indexOfLast { it.visible }

        val focusManager = LocalFocusManager.current

        Layout(
            content = {
                allItems.forEachIndexed { index, itemData ->
                    key(itemData.key ?: index) {
                        val isFirst = index == firstVisibleIndex || (index == 0 && !itemData.visible)
                        val isLast = index == lastVisibleIndex || (index == allItems.lastIndex && !itemData.visible)

                        val baseTopRadius = if (isFirst) outerCornerRadius else connectionRadius
                        val baseBottomRadius = if (isLast) outerCornerRadius else connectionRadius

                        val targetTopRadius = if (itemData.forceFlatTop) 0.dp else baseTopRadius
                        val targetBottomRadius = if (itemData.forceFlatBottom) 0.dp else baseBottomRadius

                        val isDynamicDpSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                        val currentTopRadius = (
                            if (isDynamicDpSupported) {
                                animateDpAsState(targetTopRadius, dpSpring, label = "TopRadius").value
                            } else {
                                targetTopRadius
                            }
                        ).coerceAtLeast(0.dp)

                        val currentBottomRadius = (
                            if (isDynamicDpSupported) {
                                animateDpAsState(targetBottomRadius, dpSpring, label = "BottomRadius").value
                            } else {
                                targetBottomRadius
                            }
                        ).coerceAtLeast(0.dp)

                        val shape = RoundedCornerShape(
                            topStart = currentTopRadius,
                            topEnd = currentTopRadius,
                            bottomStart = currentBottomRadius,
                            bottomEnd = currentBottomRadius,
                        )

                        val targetTopPadding = itemData.customTopPadding ?: (if (isFirst) 0.dp else gap)
                        val currentTopPadding = (
                            if (isDynamicDpSupported) {
                                animateDpAsState(targetTopPadding, dpSpring, label = "TopPadding").value
                            } else {
                                targetTopPadding
                            }
                        ).coerceAtLeast(0.dp)

                        var hasFocus by remember { mutableStateOf(false) }

                        LaunchedEffect(itemData.visible) {
                            if (!itemData.visible && hasFocus) {
                                focusManager.clearFocus()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .zIndex(if (itemData.visible) (allItems.size - index).toFloat() else -index.toFloat())
                                .onFocusChanged { hasFocus = it.hasFocus }
                                .semantics {
                                    if (!itemData.visible) hideFromAccessibility()
                                }
                                .graphicsLayer {
                                    val currentProgress = progresses[index].value
                                    val safeProgress = currentProgress.coerceIn(0f, 1f)
                                    val revealProgress = safeProgress * safeProgress * (3f - 2f * safeProgress)

                                    clip = true
                                    this.shape = object : Shape {
                                        override fun createOutline(
                                            size: Size,
                                            layoutDirection: LayoutDirection,
                                            density: Density,
                                        ) = Outline.Rectangle(Rect(0f, 0f, size.width, size.height * revealProgress))
                                    }
                                    alpha = (currentProgress * 1.5f).coerceIn(0f, 1f)
                                },
                        ) {
                            CompositionLocalProvider(LocalSegmentedItemShape provides shape) {
                                Column(modifier = Modifier.padding(top = currentTopPadding)) {
                                    itemData.content(shape)
                                }
                            }
                        }
                    }
                }
            },
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints) }
            val advances = canonicalSegmentedHeightAdvances(
                heights = FloatArray(placeables.size) { placeables[it].height.toFloat() },
                progresses = FloatArray(placeables.size) { progresses[it].value },
                reboundLimit = 24.dp.toPx(),
            )
            var currentY = 0f
            val positions = mutableListOf<Int>()

            placeables.indices.forEach { index ->
                positions.add(currentY.roundToInt())
                currentY += advances[index]
            }

            layout(constraints.maxWidth, currentY.roundToInt().coerceAtLeast(0)) {
                placeables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(x = 0, y = positions[index])
                }
            }
        }
    }
}

/**
 * Nonnegative layout advances with a single soft rebound budget for the whole group.
 */
private fun canonicalSegmentedHeightAdvances(
    heights: FloatArray,
    progresses: FloatArray,
    reboundLimit: Float,
): FloatArray {
    if (heights.isEmpty()) return FloatArray(0)
    val overflow = FloatArray(heights.size)
    val advances = FloatArray(heights.size)
    var totalOverflow = 0.0
    heights.indices.forEach { index ->
        val height = heights[index]
        val progress = progresses[index]
        val within = progress.coerceIn(0f, 1f)
        advances[index] = height * within * within * (3f - 2f * within)
        val beyond = progress - within
        overflow[index] = height * 3f * beyond * beyond
        totalOverflow += overflow[index]
    }
    if (totalOverflow > 0.0 && reboundLimit > 0f) {
        val rebound = -reboundLimit * expm1(-totalOverflow / reboundLimit)
        val scale = (rebound / totalOverflow).toFloat()
        advances.indices.forEach { advances[it] += overflow[it] * scale }
    }
    return advances
}

/**
 * Authentic Preference Tile designed for [CanonicalSegmentedColumn].
 * Automatically wraps itself in a [Surface] matching the dynamic [shape] provided by [LocalSegmentedItemShape].
 */
@Composable
fun CanonicalPreferenceTile(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.cardContainerColor,
    shape: Shape = LocalSegmentedItemShape.current,
    trailingContent: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        val clickModifier = if (onClick != null && enabled) {
            Modifier
                .clip(shape)
                .clickable(
                    role = role,
                    onClickLabel = onClickLabel,
                    onClick = onClick
                )
        } else {
            Modifier
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .then(clickModifier)
                .padding(
                    horizontal = SystemLayoutTokens.CardInternalPadding,
                    vertical = SpacingTokens.MediumSmall
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional Leading Icon (24dp glyph, 16dp gap). Left null in canonical AOSP settings subpages.
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (enabled) leadingIconTint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(SystemLayoutTokens.IconSize)
                )
                Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))
            }

            // Title & Supporting text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }
                    )
                }
            }

            // Trailing Content / Navigation Chevron
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))
                CompositionLocalProvider(
                    LocalContentColor provides if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                ) {
                    trailingContent()
                }
            } else if (showChevron) {
                Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Authentic Switch Preference Tile for [CanonicalSegmentedColumn].
 */
@Composable
fun CanonicalSwitchPreferenceTile(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.cardContainerColor,
    shape: Shape = LocalSegmentedItemShape.current,
    enabled: Boolean = true
) {
    CanonicalPreferenceTile(
        title = title,
        subtitle = subtitle,
        leadingIcon = leadingIcon,
        leadingIconTint = leadingIconTint,
        containerColor = containerColor,
        shape = shape,
        enabled = enabled,
        role = Role.Switch,
        onClickLabel = if (checked) "Turn off $title" else "Turn on $title",
        onClick = { if (enabled) onCheckedChange(!checked) },
        trailingContent = {
            BypassSwitch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled
            )
        },
        modifier = modifier
    )
}

/**
 * Authentic Two-Target Preference Tile for [CanonicalSegmentedColumn].
 * Left side: clickable area (title, subtitle, icon).
 * Right side: independent switch toggle, separated by a 32dp vertical divider.
 */
@Composable
fun CanonicalTwoTargetPreferenceTile(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTitleClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.cardContainerColor,
    shape: Shape = LocalSegmentedItemShape.current,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action Target
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onTitleClick
                    )
                    .padding(
                        horizontal = SystemLayoutTokens.CardInternalPadding,
                        vertical = SpacingTokens.MediumSmall
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (enabled) leadingIconTint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(SystemLayoutTokens.IconSize)
                    )
                    Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            }
                        )
                    }
                }
            }

            // Vertical Separator (32dp height)
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // Right Action Target: Toggle switch
            Box(
                modifier = Modifier.padding(horizontal = SystemLayoutTokens.CardInternalPadding),
                contentAlignment = Alignment.Center
            ) {
                BypassSwitch(
                    checked = checked,
                    onCheckedChange = if (enabled) onCheckedChange else null,
                    enabled = enabled
                )
            }
        }
    }
}

/**
 * Authentic AOSP / Pixel Settings Footer Info (FooterPreference pattern).
 * Displays an outlined info icon and explanatory footnote at the bottom of a settings subpage.
 */
@Composable
fun CanonicalFooterInfo(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = SystemLayoutTokens.ScreenHorizontalPadding + SpacingTokens.ExtraSmall,
                end = SystemLayoutTokens.ScreenHorizontalPadding + SpacingTokens.ExtraSmall,
                top = SpacingTokens.Large,
                bottom = SpacingTokens.Medium
            )
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(SpacingTokens.Small))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
