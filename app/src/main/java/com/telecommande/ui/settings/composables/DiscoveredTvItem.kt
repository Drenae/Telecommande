package com.telecommande.ui.settings.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.ui.theme.TvManagementSpecs

@Composable
fun DiscoveredTvItem(
    tv: DiscoveredTv,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = TvManagementSpecs.ListItemVerticalPadding,
                horizontal = TvManagementSpecs.ListItemHorizontalPadding
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TvManagementSpecs.ListItemInternalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = "Discovered TV",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(TvManagementSpecs.ListItemSpacerWidth))
            Column {
                Text(
                    text = tv.friendlyName ?: "Appareil Inconnu",
                    style = MaterialTheme.typography.titleMedium
                )
                tv.ipAddress?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}