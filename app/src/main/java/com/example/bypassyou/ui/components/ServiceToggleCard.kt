package com.example.bypassyou.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.bypassyou.R

/**
 * Hero Service Toggle Card powered by [CanonicalMainSwitchBar].
 */
@Composable
fun ServiceToggleCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    CanonicalMainSwitchBar(
        title = if (isEnabled) stringResource(R.string.service_active_title) else stringResource(R.string.service_paused_title),
        subtitle = if (isEnabled) stringResource(R.string.service_active_subtitle) else stringResource(R.string.service_paused_subtitle),
        icon = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
        checked = isEnabled,
        onCheckedChange = onToggle,
        modifier = modifier
    )
}
