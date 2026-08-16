package com.example.bypassyou.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.example.bypassyou.ui.theme.cardContainerColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServiceToggleCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBgColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "statusBgColor"
    )

    val statusDotColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "statusDotColor"
    )

    val iconTintColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconTintColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .expressiveClickable { onToggle(!isEnabled) },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isEnabled) {
                        CircularWavyProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(statusBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = iconTintColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEnabled) "Bypass Active" else "Bypass Paused",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isEnabled) "VIP calls bypass Mute/Vibrate mode with sound" else "Standard system silent mode active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}
