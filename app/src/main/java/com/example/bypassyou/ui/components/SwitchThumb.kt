package com.example.bypassyou.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SwitchThumb(checked: Boolean) {
    AnimatedContent(
        targetState = checked,
        transitionSpec = {
            scaleIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) togetherWith
                    scaleOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
        },
        label = "switch_thumb_icon"
    ) { isChecked ->
        Icon(
            imageVector = if (isChecked) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize)
        )
    }
}

@Composable
fun BypassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = { SwitchThumb(checked = checked) },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedIconColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedIconColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )
}
