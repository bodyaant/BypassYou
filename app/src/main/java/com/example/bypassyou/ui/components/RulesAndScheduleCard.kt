package com.example.bypassyou.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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
import java.util.Locale

/**
 * Unified Android 16 / M3 Expressive "Sound & schedule" section combining:
 * 1. Emergency Ringer Volume Slider ([VolumeSettingSegment])
 * 2. Gentle Volume Ramp-Up with Expressive connected duration button group
 * 3. Active Schedule with tonal time cards and 7 compact morphing day-of-week toggles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundAndScheduleSection(
    volumePercent: Int,
    maxSteps: Int,
    minSteps: Int,
    onVolumeChange: (Int) -> Unit,
    onTestSound: () -> Unit,
    isAscendingEnabled: Boolean,
    ascendingDurationSec: Int,
    onToggleAscending: (Boolean) -> Unit,
    onSetAscendingDuration: (Int) -> Unit,
    isScheduleEnabled: Boolean,
    scheduleStartHour: Int,
    scheduleStartMinute: Int,
    scheduleEndHour: Int,
    scheduleEndMinute: Int,
    scheduleDays: Set<String>,
    onToggleSchedule: (Boolean) -> Unit,
    onSetScheduleTime: (Int, Int, Int, Int) -> Unit,
    onToggleScheduleDay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CanonicalSegmentedColumn(
        title = stringResource(R.string.sound_and_rules_title),
        modifier = modifier
    ) {
        // 1. Ringer Volume Segment
        item(key = "sound_volume") { shape ->
            VolumeSettingSegment(
                volumePercent = volumePercent,
                maxSteps = maxSteps,
                minSteps = minSteps,
                onVolumeChange = onVolumeChange,
                onTestSound = onTestSound,
                shape = shape
            )
        }

        // 2. Gentle Volume Ramp-Up Segment
        item(key = "rules_ascending") { shape ->
            AscendingVolumeSegment(
                isAscendingEnabled = isAscendingEnabled,
                ascendingDurationSec = ascendingDurationSec,
                onToggleAscending = onToggleAscending,
                onSetAscendingDuration = onSetAscendingDuration,
                shape = shape
            )
        }

        // 3. Active Schedule Segment
        item(key = "rules_schedule") { shape ->
            ScheduleSegment(
                isScheduleEnabled = isScheduleEnabled,
                scheduleStartHour = scheduleStartHour,
                scheduleStartMinute = scheduleStartMinute,
                scheduleEndHour = scheduleEndHour,
                scheduleEndMinute = scheduleEndMinute,
                scheduleDays = scheduleDays,
                onToggleSchedule = onToggleSchedule,
                onSetScheduleTime = onSetScheduleTime,
                onToggleScheduleDay = onToggleScheduleDay,
                shape = shape
            )
        }
    }
}

@Composable
fun AscendingVolumeSegment(
    isAscendingEnabled: Boolean,
    ascendingDurationSec: Int,
    onToggleAscending: (Boolean) -> Unit,
    onSetAscendingDuration: (Int) -> Unit,
    shape: Shape = LocalSegmentedItemShape.current,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val durations = listOf(3, 5, 8, 12)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CanonicalSwitchPreferenceTile(
                title = stringResource(R.string.rules_ascending_title),
                subtitle = stringResource(R.string.rules_ascending_desc, ascendingDurationSec),
                leadingIcon = null,
                checked = isAscendingEnabled,
                onCheckedChange = onToggleAscending,
                containerColor = Color.Transparent,
                shape = RectangleShape
            )

            AnimatedVisibility(
                visible = isAscendingEnabled,
                enter = expandVertically(animationSpec = MotionTokens.Springs.standard()) + fadeIn(),
                exit = shrinkVertically(animationSpec = MotionTokens.Springs.standard()) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SystemLayoutTokens.CardInternalPadding,
                            end = SystemLayoutTokens.CardInternalPadding,
                            bottom = SpacingTokens.Medium
                        ),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.ExtraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    durations.forEachIndexed { index, sec ->
                        val isSelected = ascendingDurationSec == sec
                        val animatedWeight by animateFloatAsState(
                            targetValue = if (isSelected) 1.25f else 1.0f,
                            animationSpec = MotionTokens.Springs.expressiveBouncy(),
                            label = "AscendingWeight_$sec"
                        )
                        val startRadius by animateDpAsState(
                            targetValue = if (isSelected || index == 0) 20.dp else 6.dp,
                            animationSpec = MotionTokens.Springs.expressiveBouncy(),
                            label = "AscendingStartRadius_$sec"
                        )
                        val endRadius by animateDpAsState(
                            targetValue = if (isSelected || index == durations.lastIndex) 20.dp else 6.dp,
                            animationSpec = MotionTokens.Springs.expressiveBouncy(),
                            label = "AscendingEndRadius_$sec"
                        )
                        val containerColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            label = "AscendingColor_$sec"
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            label = "AscendingContentColor_$sec"
                        )

                        Surface(
                            onClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetAscendingDuration(sec)
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
                                .height(40.dp)
                                .semantics {
                                    role = Role.RadioButton
                                    selected = isSelected
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.rules_seconds_short, sec),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSegment(
    isScheduleEnabled: Boolean,
    scheduleStartHour: Int,
    scheduleStartMinute: Int,
    scheduleEndHour: Int,
    scheduleEndMinute: Int,
    scheduleDays: Set<String>,
    onToggleSchedule: (Boolean) -> Unit,
    onSetScheduleTime: (Int, Int, Int, Int) -> Unit,
    onToggleScheduleDay: (String) -> Unit,
    shape: Shape = LocalSegmentedItemShape.current,
    modifier: Modifier = Modifier
) {
    var showTimePickerForStart by remember { mutableStateOf(false) }
    var showTimePickerForEnd by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val daysOfWeek = listOf(
        "1" to stringResource(R.string.day_mon),
        "2" to stringResource(R.string.day_tue),
        "3" to stringResource(R.string.day_wed),
        "4" to stringResource(R.string.day_thu),
        "5" to stringResource(R.string.day_fri),
        "6" to stringResource(R.string.day_sat),
        "7" to stringResource(R.string.day_sun)
    )

    val startTimeFormatted = String.format(Locale.ROOT, "%02d:%02d", scheduleStartHour, scheduleStartMinute)
    val endTimeFormatted = String.format(Locale.ROOT, "%02d:%02d", scheduleEndHour, scheduleEndMinute)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CanonicalSwitchPreferenceTile(
                title = stringResource(R.string.rules_schedule_title),
                subtitle = if (isScheduleEnabled) stringResource(R.string.rules_schedule_active_desc, startTimeFormatted, endTimeFormatted)
                else stringResource(R.string.rules_schedule_always_desc),
                leadingIcon = null,
                checked = isScheduleEnabled,
                onCheckedChange = onToggleSchedule,
                containerColor = Color.Transparent,
                shape = RectangleShape
            )

            AnimatedVisibility(
                visible = isScheduleEnabled,
                enter = expandVertically(animationSpec = MotionTokens.Springs.standard()) + fadeIn(),
                exit = shrinkVertically(animationSpec = MotionTokens.Springs.standard()) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SystemLayoutTokens.CardInternalPadding,
                            end = SystemLayoutTokens.CardInternalPadding,
                            bottom = SpacingTokens.Medium
                        )
                ) {
                    // Tonal Start & End Time Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
                    ) {
                        Surface(
                            onClick = { showTimePickerForStart = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SpacingTokens.Medium, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.rules_schedule_start_btn),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = startTimeFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            onClick = { showTimePickerForEnd = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SpacingTokens.Medium, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.rules_schedule_end_btn),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = endTimeFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SpacingTokens.Small))

                    // 7 Compact Expressive Day-of-Week Toggle Buttons (no overflow on narrow screens)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        daysOfWeek.forEach { (key, label) ->
                            val isSelected = scheduleDays.contains(key)
                            val cornerRadius by animateDpAsState(
                                targetValue = if (isSelected) 20.dp else 10.dp,
                                animationSpec = MotionTokens.Springs.expressiveBouncy(),
                                label = "DayCornerRadius_$key"
                            )
                            val containerColor by animateColorAsState(
                                targetValue = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                label = "DayContainerColor_$key"
                            )
                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                label = "DayContentColor_$key"
                            )

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleScheduleDay(key)
                                },
                                shape = RoundedCornerShape(cornerRadius),
                                color = containerColor,
                                contentColor = contentColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .semantics {
                                        role = Role.Checkbox
                                        selected = isSelected
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTimePickerForStart) {
        val startPickerState = rememberTimePickerState(
            initialHour = scheduleStartHour,
            initialMinute = scheduleStartMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePickerForStart = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetScheduleTime(
                            startPickerState.hour,
                            startPickerState.minute,
                            scheduleEndHour,
                            scheduleEndMinute
                        )
                        showTimePickerForStart = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerForStart = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            title = { Text(stringResource(R.string.rules_dialog_start_title)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = startPickerState)
                }
            }
        )
    }

    if (showTimePickerForEnd) {
        val endPickerState = rememberTimePickerState(
            initialHour = scheduleEndHour,
            initialMinute = scheduleEndMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePickerForEnd = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetScheduleTime(
                            scheduleStartHour,
                            scheduleStartMinute,
                            endPickerState.hour,
                            endPickerState.minute
                        )
                        showTimePickerForEnd = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerForEnd = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            title = { Text(stringResource(R.string.rules_dialog_end_title)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = endPickerState)
                }
            }
        )
    }
}

/**
 * Legacy wrapper for [RulesAndScheduleCard] compatibility.
 */
@Composable
fun RulesAndScheduleCard(
    isStarredEnabled: Boolean,
    starredCount: Int,
    onToggleStarred: (Boolean) -> Unit,
    isAscendingEnabled: Boolean,
    ascendingDurationSec: Int,
    onToggleAscending: (Boolean) -> Unit,
    onSetAscendingDuration: (Int) -> Unit,
    isScheduleEnabled: Boolean,
    scheduleStartHour: Int,
    scheduleStartMinute: Int,
    scheduleEndHour: Int,
    scheduleEndMinute: Int,
    scheduleDays: Set<String>,
    onToggleSchedule: (Boolean) -> Unit,
    onSetScheduleTime: (Int, Int, Int, Int) -> Unit,
    onToggleScheduleDay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val starredTitle = stringResource(R.string.rules_starred_title)
    val starredSubtitle = if (starredCount > 0) stringResource(R.string.rules_starred_desc_with_count, starredCount)
    else stringResource(R.string.rules_starred_desc)

    CanonicalSegmentedColumn(
        title = stringResource(R.string.rules_card_title),
        modifier = modifier
    ) {
        switchItem(
            key = "rules_starred",
            title = starredTitle,
            subtitle = starredSubtitle,
            checked = isStarredEnabled,
            onCheckedChange = onToggleStarred
        )

        item(key = "rules_ascending") { shape ->
            AscendingVolumeSegment(
                isAscendingEnabled = isAscendingEnabled,
                ascendingDurationSec = ascendingDurationSec,
                onToggleAscending = onToggleAscending,
                onSetAscendingDuration = onSetAscendingDuration,
                shape = shape
            )
        }

        item(key = "rules_schedule") { shape ->
            ScheduleSegment(
                isScheduleEnabled = isScheduleEnabled,
                scheduleStartHour = scheduleStartHour,
                scheduleStartMinute = scheduleStartMinute,
                scheduleEndHour = scheduleEndHour,
                scheduleEndMinute = scheduleEndMinute,
                scheduleDays = scheduleDays,
                onToggleSchedule = onToggleSchedule,
                onSetScheduleTime = onSetScheduleTime,
                onToggleScheduleDay = onToggleScheduleDay,
                shape = shape
            )
        }
    }
}
