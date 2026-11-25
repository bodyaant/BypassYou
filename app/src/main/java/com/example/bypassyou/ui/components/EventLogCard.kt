package com.example.bypassyou.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bypassyou.R
import com.example.bypassyou.data.model.CallEventLog
import com.example.bypassyou.ui.theme.cardContainerColor
import com.example.bypassyou.ui.tokens.MotionTokens
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Canonical Android 16 Call Activity & Audit Log Segment / Card.
 * Collapsible preference segment displaying recent call detections and bypass actions.
 */
@Composable
fun EventLogCard(
    eventLogs: List<CallEventLog>,
    onAddVipFromLog: (CallEventLog) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CanonicalOuterCornerRadius)
) {
    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = MotionTokens.Springs.standard(),
        label = "event_log_chevron_rotation"
    )

    val timeFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .clickable(
                        role = Role.Button,
                        onClick = { isExpanded = !isExpanded }
                    )
                    .padding(
                        horizontal = SystemLayoutTokens.CardInternalPadding,
                        vertical = SpacingTokens.MediumSmall
                    )
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.event_log_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (eventLogs.isEmpty()) stringResource(R.string.event_log_empty_summary)
                        else stringResource(R.string.event_log_count_summary, eventLogs.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (eventLogs.isNotEmpty() && isExpanded) {
                        IconButton(
                            onClick = onClearLogs,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.event_log_clear_cd),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = MotionTokens.Springs.standard()) + fadeIn(),
                exit = shrinkVertically(animationSpec = MotionTokens.Springs.standard()) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SystemLayoutTokens.CardInternalPadding)
                        .padding(bottom = SystemLayoutTokens.CardInternalPadding)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(SpacingTokens.Small))

                    if (eventLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SpacingTokens.Medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.event_log_empty_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        eventLogs.take(15).forEachIndexed { index, log ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 52.dp, top = 4.dp, bottom = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            }
                            EventLogRow(
                                log = log,
                                timeFormatted = timeFormat.format(Date(log.timestamp)),
                                onAddVip = { onAddVipFromLog(log) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventLogRow(
    log: CallEventLog,
    timeFormatted: String,
    onAddVip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (log.isBypassed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (log.isBypassed) Icons.Default.PhoneInTalk else Icons.Default.CallEnd,
                        contentDescription = null,
                        tint = if (log.isBypassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.contactName.ifBlank { log.phoneNumber },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (log.isBypassed) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = if (log.isBypassed) "${log.reason} (${log.volumePercent}%)" else log.reason,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (log.isBypassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (!log.isBypassed) {
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalIconButton(
                onClick = onAddVip,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = stringResource(R.string.event_log_add_vip_cd),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
