package com.telecommande.ui.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.ui.theme.AppColors
import com.telecommande.util.outerRoundedShadow

@Composable
fun DiscoveredTvItem(
    tv: DiscoveredTv,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = 18.dp,
                color = Color.Black,
                alpha = 0.55f,
                blurRadius = 5.dp,
                offsetY = 2.dp
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.surfaceElevated,
                        AppColors.surface,
                        AppColors.remoteDeep
                    )
                ),
                shape
            )
            .border(
                width = 1.dp,
                color = AppColors.border.copy(alpha = 0.9f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(AppColors.accentMuted.copy(alpha = 0.75f), CircleShape)
                .border(
                    width = 1.dp,
                    color = AppColors.accent.copy(alpha = 0.35f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tv,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tv.friendlyName ?: "Appareil inconnu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.appWhite
            )

            tv.ipAddress?.let { ip ->
                Text(
                    text = ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(AppColors.accentMuted, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Appairer cette TV",
                tint = AppColors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
