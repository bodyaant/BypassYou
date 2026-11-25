package com.example.bypassyou.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bypassyou.R
import com.example.bypassyou.ui.theme.cardContainerColor
import com.example.bypassyou.ui.tokens.MotionTokens
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens

private data class ThemeOption(
    val id: String,
    val title: String,
    val icon: ImageVector
)

/**
 * M3 Expressive Theme Selector Segment (ported from M3DEGallery's ExpressiveThemeSwitch).
 * Uses elastic spring weight expansion and morphing corner radii (24dp active pill / 6dp inner corners).
 */
@Composable
fun ThemeSelectorSegment(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    shape: Shape = LocalSegmentedItemShape.current,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val options = listOf(
        ThemeOption("SYSTEM", stringResource(R.string.theme_system), Icons.Default.SettingsBrightness),
        ThemeOption("LIGHT", stringResource(R.string.theme_light), Icons.Default.LightMode),
        ThemeOption("DARK", stringResource(R.string.theme_dark), Icons.Default.DarkMode)
    )

    val selectedIndex = when (currentTheme) {
        "LIGHT" -> 1
        "DARK" -> 2
        else -> 0
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SystemLayoutTokens.CardInternalPadding,
                    vertical = SpacingTokens.MediumSmall
                )
        ) {
            Text(
                text = stringResource(R.string.theme_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.theme_card_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.ExtraSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = selectedIndex == index

                    val animatedWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = MotionTokens.Springs.expressiveBouncy(),
                        label = "ThemeSegmentWeight_$index"
                    )

                    val maxRadius = 22.dp
                    val innerRadius = 6.dp

                    val startRadius by animateDpAsState(
                        targetValue = if (isSelected || index == 0) maxRadius else innerRadius,
                        animationSpec = MotionTokens.Springs.expressiveBouncy(),
                        label = "ThemeStartRadius_$index"
                    )
                    val endRadius by animateDpAsState(
                        targetValue = if (isSelected || index == options.lastIndex) maxRadius else innerRadius,
                        animationSpec = MotionTokens.Springs.expressiveBouncy(),
                        label = "ThemeEndRadius_$index"
                    )

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        label = "ThemeContainerColor_$index"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "ThemeContentColor_$index"
                    )

                    Surface(
                        onClick = {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onThemeSelected(option.id)
                            }
                        },
                        shape = RoundedCornerShape(
                            topStart = startRadius,
                            bottomStart = startRadius,
                            topEnd = endRadius,
                            bottomEnd = endRadius
                        ),
                        color = containerColor,
                        contentColor = contentColor,
                        modifier = Modifier
                            .weight(animatedWeight)
                            .height(44.dp)
                            .semantics {
                                role = Role.RadioButton
                                selected = isSelected
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = SpacingTokens.ExtraSmall)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

