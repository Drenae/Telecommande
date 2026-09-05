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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.theme.AppColors
import com.telecommande.util.outerRoundedShadow

@Composable
fun PairedTvItem(
    tvInfo: PairedTvInfo,
    displayName: String,
    isActive: Boolean,
    isConnectedToThisTv: Boolean,
    onConnectClick: () -> Unit,
    onRenameClick: () -> Unit,
    onForgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        isConnectedToThisTv -> AppColors.statusGreen
        isActive -> AppColors.statusAmber
        else -> AppColors.textSecondary
    }
    val technicalName = tvInfo.name ?: tvInfo.ipAddress
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = 18.dp,
                color = Color.Black,
                alpha = 0.58f,
                blurRadius = 5.dp,
                offsetY = 2.dp
            )
            .background(
                Brush.linearGradient(
                    if (isActive) {
                        listOf(
                            AppColors.accentMuted.copy(alpha = 0.9f),
                            AppColors.surface,
                            AppColors.remoteDeep
                        )
                    } else {
                        listOf(
                            AppColors.surfaceElevated,
                            AppColors.surface,
                            AppColors.remoteDeep
                        )
                    }
                ),
                shape
            )
            .border(
                width = 1.dp,
                color = if (isActive) {
                    AppColors.accent.copy(alpha = 0.65f)
                } else {
                    AppColors.border.copy(alpha = 0.9f)
                },
                shape = shape
            )
            .clickable(onClick = onConnectClick)
            .padding(start = 14.dp, top = 13.dp, bottom = 13.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (isActive) {
                        AppColors.accentMuted
                    } else {
                        AppColors.surfacePressed
                    },
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (isActive) {
                        AppColors.accent.copy(alpha = 0.5f)
                    } else {
                        AppColors.border
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tv,
                contentDescription = null,
                tint = if (isActive) AppColors.accent else AppColors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.appWhite
            )

            if (displayName != technicalName) {
                Text(
                    text = technicalName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }

            Text(
                text = tvInfo.ipAddress,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary
            )

            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 5.dp)
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
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
        }

        IconButton(onClick = onRenameClick) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(AppColors.accentMuted.copy(alpha = 0.7f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Renommer $displayName",
                    tint = AppColors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        IconButton(onClick = onForgetClick) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(AppColors.statusRed.copy(alpha = 0.1f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Oublier $displayName",
                    tint = AppColors.statusRed.copy(alpha = 0.9f),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
