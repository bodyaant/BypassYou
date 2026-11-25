// SPDX-License-Identifier: Apache-2.0
// Ported from microg-RE (AOSP SettingsLib MainSwitchPreference pattern)
package com.example.bypassyou.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.bypassyou.ui.tokens.MotionTokens
import com.example.bypassyou.ui.tokens.ShapeTokens
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens

/**
 * Authentic AOSP / Pixel Settings Main Switch Bar (Pill Switch).
 *
 * Modeled after AOSP SettingsLib `MainSwitchPreference` and microg-RE's `preference_material_switch_bar.xml`.
 * Serves as the prominent, primary toggle on a settings subpage (e.g. "Use dark theme", "Use location").
 *
 * Visual characteristics:
 * - Fully rounded pill container (ShapeTokens.Full / 999.dp).
 * - Animated background: [primaryContainer] when active (ON), [surfaceContainerHighest] when inactive (OFF).
 * - Animated text color: [onPrimaryContainer] when active, [onSurface] when inactive.
 * - Whole pill is clickable with ripple feedback.
 * - Embedded MaterialSwitch with thumb check/close glyphs.
 */
@Composable
fun CanonicalMainSwitchBar(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    shape: Shape = ShapeTokens.Full,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Dynamic animated container colors based on state
    val targetContainerColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.38f)
        checked -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val targetTitleColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        checked -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val targetSubtitleColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        checked -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = MotionTokens.Springs.expressiveFastBouncy(),
        label = "MainSwitchBarContainerColor"
    )

    val titleColor by animateColorAsState(
        targetValue = targetTitleColor,
        animationSpec = MotionTokens.Springs.expressiveFastBouncy(),
        label = "MainSwitchBarTitleColor"
    )

    val subtitleColor by animateColorAsState(
        targetValue = targetSubtitleColor,
        animationSpec = MotionTokens.Springs.expressiveFastBouncy(),
        label = "MainSwitchBarSubtitleColor"
    )

    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SystemLayoutTokens.ScreenHorizontalPadding),
        enabled = enabled,
        shape = shape,
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional Leading Icon
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(SystemLayoutTokens.IconSize)
                )
                Spacer(modifier = Modifier.width(SpacingTokens.Medium))
            }

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.Medium))

            // Switch Widget with animated glyphs
            Box(contentAlignment = Alignment.Center) {
                BypassSwitch(
                    checked = checked,
                    onCheckedChange = if (enabled) onCheckedChange else null,
                    enabled = enabled
                )
            }
        }
    }
}
