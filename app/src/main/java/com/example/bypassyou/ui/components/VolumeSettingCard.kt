package com.example.bypassyou.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bypassyou.R
import com.example.bypassyou.ui.theme.cardContainerColor
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens
import kotlin.math.roundToInt

/**
 * Canonical Emergency Volume segment content for use inside a [CanonicalSegmentedColumn].
 * Features a compact header row with percentage badge, inline test-sound icon button, and discrete M3 [Slider].
 */
@Composable
fun VolumeSettingSegment(
    volumePercent: Int,
    maxSteps: Int = 15,
    minSteps: Int = 0,
    onVolumeChange: (Int) -> Unit,
    onTestSound: () -> Unit,
    shape: Shape = LocalSegmentedItemShape.current,
    modifier: Modifier = Modifier
) {
    val safeMaxSteps = if (maxSteps <= 0) 15 else maxSteps
    val safeMinSteps = minSteps.coerceIn(0, safeMaxSteps - 1)
    val currentStep = ((volumePercent / 100f) * safeMaxSteps).roundToInt().coerceIn(safeMinSteps, safeMaxSteps)
    val stepsCount = (safeMaxSteps - safeMinSteps - 1).coerceAtLeast(0)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.volume_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.volume_card_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(SpacingTokens.Small))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.ExtraSmall)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$volumePercent%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onTestSound,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.volume_test_sound_btn)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))

            Slider(
                value = currentStep.toFloat(),
                onValueChange = { newStepFloat ->
                    val newStep = newStepFloat.roundToInt().coerceIn(safeMinSteps, safeMaxSteps)
                    val newPercent = ((newStep.toFloat() / safeMaxSteps.toFloat()) * 100f).roundToInt()
                    onVolumeChange(newPercent)
                },
                valueRange = safeMinSteps.toFloat()..safeMaxSteps.toFloat(),
                steps = stepsCount,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Standalone wrapper around [VolumeSettingSegment] for backwards compatibility.
 */
@Composable
fun VolumeSettingCard(
    volumePercent: Int,
    maxSteps: Int = 15,
    minSteps: Int = 0,
    onVolumeChange: (Int) -> Unit,
    onTestSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanonicalSegmentedColumn(
        modifier = modifier
    ) {
        item(key = "volume_setting_card") { shape ->
            VolumeSettingSegment(
                volumePercent = volumePercent,
                maxSteps = maxSteps,
                minSteps = minSteps,
                onVolumeChange = onVolumeChange,
                onTestSound = onTestSound,
                shape = shape
            )
        }
    }
}
