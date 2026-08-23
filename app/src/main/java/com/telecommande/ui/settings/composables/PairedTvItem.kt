package com.telecommande.ui.settings.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.theme.AppColors

@Composable
fun PairedTvItem(
    tvInfo: PairedTvInfo,
    isActive: Boolean,
    isConnectedToThisTv: Boolean,
    onConnectClick: () -> Unit,
    onForgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        isConnectedToThisTv -> AppColors.statusGreen
        isActive -> AppColors.statusAmber
        else -> AppColors.textSecondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onConnectClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) AppColors.accent.copy(alpha = 0.55f) else AppColors.border
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) AppColors.accentMuted.copy(alpha = 0.55f) else AppColors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = if (isActive) AppColors.accent else AppColors.textSecondary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tvInfo.name ?: "TV appairée",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.appWhite
                )
                Text(
                    text = tvInfo.ipAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                if (isActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isConnectedToThisTv) "Connectée" else "TV active",
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor
                        )
                    }
                }
            }

            IconButton(onClick = onForgetClick) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Oublier ${tvInfo.name ?: "la TV"}",
                    tint = AppColors.statusRed.copy(alpha = 0.85f)
                )
            }
        }
    }
}