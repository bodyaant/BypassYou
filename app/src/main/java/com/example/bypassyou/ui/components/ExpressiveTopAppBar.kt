package com.example.bypassyou.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.bypassyou.R

/**
 * Standardized circular back button matching Android system settings and InstallerX-Revived.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentDescription: String = stringResource(R.string.action_back),
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shapes = IconButtonDefaults.shapes(
            shape = CircleShape,
        ),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

/**
 * Circular action icon button matching [ExpressiveBackButton] styling.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentDescription: String? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        shapes = IconButtonDefaults.shapes(
            shape = CircleShape,
        ),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

/**
 * Canonical M3 Expressive Large Top App Bar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CanonicalLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    titleHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    LargeFlexibleTopAppBar(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
        colors = colors,
        titleHorizontalAlignment = titleHorizontalAlignment,
    )
}
