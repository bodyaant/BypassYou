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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.bypassyou.R
import com.example.bypassyou.ui.theme.cardContainerColor
import com.example.bypassyou.ui.tokens.MotionTokens
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens

/**
 * Collapsible System Health segment for embedding inside the "Activity & system" [CanonicalSegmentedColumn]
 * when all permissions are healthy, or expandable on demand.
 */
@Composable
fun SystemHealthCollapsibleSegment(
    isIgnoringBatteryOpt: Boolean,
    isDndAccessGranted: Boolean,
    isCallScreeningRoleHeld: Boolean,
    hasPhonePermissions: Boolean,
    onRequestBatteryOpt: () -> Unit,
    onRequestDndAccess: () -> Unit,
    onRequestCallScreeningRole: () -> Unit,
    onRequestPhonePermissions: () -> Unit,
    shape: Shape = LocalSegmentedItemShape.current,
    modifier: Modifier = Modifier
) {
    val allOptimal = isIgnoringBatteryOpt && isDndAccessGranted && isCallScreeningRoleHeld && hasPhonePermissions
    var isExpanded by remember { mutableStateOf(!allOptimal) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = MotionTokens.Springs.standard(),
        label = "health_chevron_rotation"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
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
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.health_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (allOptimal) stringResource(R.string.health_optimal) else stringResource(R.string.health_needs_action),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (allOptimal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = if (allOptimal) stringResource(R.string.health_badge_100) else stringResource(R.string.health_badge_attention),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (allOptimal) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
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
                    Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))

                    HealthInlineRow(
                        title = stringResource(R.string.health_permissions_title),
                        subtitle = if (hasPhonePermissions) stringResource(R.string.health_permissions_optimal) else stringResource(R.string.health_permissions_restricted),
                        isOk = hasPhonePermissions,
                        actionLabel = stringResource(R.string.health_action_allow),
                        onAction = onRequestPhonePermissions
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    HealthInlineRow(
                        title = stringResource(R.string.health_dnd_title),
                        subtitle = if (isDndAccessGranted) stringResource(R.string.health_dnd_optimal) else stringResource(R.string.health_dnd_restricted),
                        isOk = isDndAccessGranted,
                        actionLabel = stringResource(R.string.health_action_configure),
                        onAction = onRequestDndAccess
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    HealthInlineRow(
                        title = stringResource(R.string.health_screening_title),
                        subtitle = if (isCallScreeningRoleHeld) stringResource(R.string.health_screening_optimal) else stringResource(R.string.health_screening_restricted),
                        isOk = isCallScreeningRoleHeld,
                        actionLabel = stringResource(R.string.health_action_enable),
                        onAction = onRequestCallScreeningRole
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                    HealthInlineRow(
                        title = stringResource(R.string.health_battery_title),
                        subtitle = if (isIgnoringBatteryOpt) stringResource(R.string.health_battery_optimal) else stringResource(R.string.health_battery_restricted),
                        isOk = isIgnoringBatteryOpt,
                        actionLabel = stringResource(R.string.health_action_allow),
                        onAction = onRequestBatteryOpt
                    )
                }
            }
        }
    }
}

/**
 * Prominent System Health & Diagnostics Card displayed when setup is incomplete (`!allOptimal`).
 */
@Composable
fun SystemHealthCard(
    isServiceActive: Boolean,
    isIgnoringBatteryOpt: Boolean,
    isDndAccessGranted: Boolean,
    isCallScreeningRoleHeld: Boolean,
    onRequestBatteryOpt: () -> Unit,
    onRequestDndAccess: () -> Unit,
    onRequestCallScreeningRole: () -> Unit,
    modifier: Modifier = Modifier,
    hasPhonePermissions: Boolean = true,
    onRequestPhonePermissions: () -> Unit = {}
) {
    val allOptimal = isIgnoringBatteryOpt && isDndAccessGranted && isCallScreeningRoleHeld && hasPhonePermissions

    CanonicalSegmentedColumn(
        title = stringResource(R.string.health_card_title),
        badgeText = if (allOptimal) stringResource(R.string.health_badge_100) else stringResource(R.string.health_badge_attention),
        modifier = modifier
    ) {
        run {
            item(key = "health_permissions", animatedVisibility = !hasPhonePermissions) { shape ->
                HealthItemSegment(
                    title = stringResource(R.string.health_permissions_title),
                    subtitle = stringResource(R.string.health_permissions_restricted),
                    isOk = false,
                    actionLabel = stringResource(R.string.health_action_allow),
                    onAction = onRequestPhonePermissions,
                    shape = shape
                )
            }
        }

        run {
            item(key = "health_dnd", animatedVisibility = !isDndAccessGranted) { shape ->
                HealthItemSegment(
                    title = stringResource(R.string.health_dnd_title),
                    subtitle = stringResource(R.string.health_dnd_restricted),
                    isOk = false,
                    actionLabel = stringResource(R.string.health_action_configure),
                    onAction = onRequestDndAccess,
                    shape = shape
                )
            }
        }

        run {
            item(key = "health_screening", animatedVisibility = !isCallScreeningRoleHeld) { shape ->
                HealthItemSegment(
                    title = stringResource(R.string.health_screening_title),
                    subtitle = stringResource(R.string.health_screening_restricted),
                    isOk = false,
                    actionLabel = stringResource(R.string.health_action_enable),
                    onAction = onRequestCallScreeningRole,
                    shape = shape
                )
            }
        }

        run {
            item(key = "health_battery", animatedVisibility = !isIgnoringBatteryOpt) { shape ->
                HealthItemSegment(
                    title = stringResource(R.string.health_battery_title),
                    subtitle = stringResource(R.string.health_battery_restricted),
                    isOk = false,
                    actionLabel = stringResource(R.string.health_action_allow),
                    onAction = onRequestBatteryOpt,
                    shape = shape
                )
            }
        }
    }
}

@Composable
private fun HealthInlineRow(
    title: String,
    subtitle: String,
    isOk: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(SpacingTokens.Small))

        if (isOk) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        } else {
            FilledTonalButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun HealthItemSegment(
    title: String,
    subtitle: String,
    isOk: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    shape: Shape
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SystemLayoutTokens.CardInternalPadding,
                    vertical = SpacingTokens.MediumSmall
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.Small))

            if (isOk) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                FilledTonalButton(
                    onClick = onAction,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
