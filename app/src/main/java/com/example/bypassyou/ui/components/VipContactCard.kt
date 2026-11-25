package com.example.bypassyou.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bypassyou.R
import com.example.bypassyou.data.model.VipContact
import com.example.bypassyou.ui.theme.cardContainerColor
import com.example.bypassyou.ui.tokens.ExpressiveShapes
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens

/**
 * Canonical Android 16 VIP Contact Item.
 * Uses official Material 3 Expressive organic avatar shapes with tonal container and 48dp accessible delete target.
 */
@Composable
fun VipContactCard(
    contact: VipContact,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = SystemLayoutTokens.CardInternalPadding,
                end = SpacingTokens.Small,
                top = 10.dp,
                bottom = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {}
        ) {
            // M3 Expressive Avatar Shape
            val avatarShape = ExpressiveShapes.getAvatarShape(contact.name)
            Surface(
                shape = avatarShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = contact.name.trim().take(1).uppercase()
                    if (initials.isNotEmpty() && initials.first().isLetter()) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.vip_delete_cd),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Canonical AOSP Two-Target "Add VIP contact" segment placed at the bottom of the "Who can reach you" group.
 * - Left target: opens system Contact Picker (1 tap).
 * - Right target (separated by a 32dp vertical divider): opens Manual Number Entry bottom sheet (1 tap).
 */
@Composable
fun AddVipContactSegment(
    onPickFromContacts: () -> Unit,
    onAddManually: () -> Unit,
    shape: Shape = LocalSegmentedItemShape.current,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.cardContainerColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Action Target: Pick from phonebook
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .clickable(
                        role = Role.Button,
                        onClick = onPickFromContacts
                    )
                    .padding(
                        horizontal = SystemLayoutTokens.CardInternalPadding,
                        vertical = SpacingTokens.MediumSmall
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(SystemLayoutTokens.IconToContentSpacing))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.fab_add_vip),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.add_from_phonebook_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Vertical Separator (32dp height)
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            // Right Action Target: Enter phone number manually
            Box(
                modifier = Modifier.padding(horizontal = SpacingTokens.Small),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalIconButton(
                    onClick = onAddManually,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.add_manually_title),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
