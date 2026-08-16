package com.example.bypassyou.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bypassyou.ui.theme.cardContainerColor

private data class ThemeOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun ThemeSelectorCard(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember {
        listOf(
            ThemeOption("SYSTEM", "System", Icons.Default.BrightnessAuto),
            ThemeOption("LIGHT", "Light", Icons.Default.LightMode),
            ThemeOption("DARK", "Dark", Icons.Default.DarkMode)
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize application appearance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = currentTheme == option.key
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val animatedWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.3f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "themeWeight_$index"
                    )

                    val startRadius by animateDpAsState(
                        targetValue = if (isSelected || index == 0) 24.dp else 10.dp,
                        label = "startR_$index"
                    )
                    val endRadius by animateDpAsState(
                        targetValue = if (isSelected || index == options.size - 1) 24.dp else 10.dp,
                        label = "endR_$index"
                    )

                    val itemShape = RoundedCornerShape(
                        topStart = startRadius,
                        bottomStart = startRadius,
                        topEnd = endRadius,
                        bottomEnd = endRadius
                    )

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        label = "themeBg_$index"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "themeContent_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(animatedWeight)
                            .fillMaxHeight()
                            .clip(itemShape)
                            .background(bgColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onThemeSelected(option.key) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
