package com.telecommande.ui.settings.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.theme.TvManagementSpecs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PairedTvItem(
    tvInfo: PairedTvInfo,
    isActive: Boolean,
    isConnectedToThisTv: Boolean,
    onConnectClick: () -> Unit,
    onForgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onConnectClick,
            )
            .padding(
                vertical = TvManagementSpecs.ListItemVerticalPadding,
                horizontal = TvManagementSpecs.ListItemHorizontalPadding
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TvManagementSpecs.ListItemInternalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "Paired TV",
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(TvManagementSpecs.ListItemSpacerWidth))
                Column {
                    Text(
                        text = tvInfo.name ?: "TV Appairée",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tvInfo.ipAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (isActive) {
                        Text(
                            text = if (isConnectedToThisTv) "Connecté" else "Défini comme actif",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnectedToThisTv) Color.Green.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isActive && isConnectedToThisTv) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Connected",
                        tint = Color.Green,
                        modifier = Modifier.size(TvManagementSpecs.PairedTvStatusIconSize)
                    )
                } else if (isActive) {
                    Icon(
                        imageVector = Icons.Default.LinkOff,
                        contentDescription = "Active but not connected",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(TvManagementSpecs.PairedTvStatusIconSize)
                    )
                }
                Spacer(modifier = Modifier.width(TvManagementSpecs.PairedTvForgetButtonSpacer))
                IconButton(onClick = onForgetClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Forget TV",
                        tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}