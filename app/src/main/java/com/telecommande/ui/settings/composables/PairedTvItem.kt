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
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.PairedTvItemDimensions
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
    val shape = RoundedCornerShape(PairedTvItemDimensions.cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = PairedTvItemDimensions.cornerRadius,
                color = Color.Black,
                alpha = 0.58f,
                blurRadius = PairedTvItemDimensions.shadowBlurRadius,
                offsetY = PairedTvItemDimensions.shadowOffsetY
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
                width = PairedTvItemDimensions.mainBorderWidth,
                color = if (isActive) {
                    AppColors.accent.copy(alpha = 0.65f)
                } else {
                    AppColors.border.copy(alpha = 0.9f)
                },
                shape = shape
            )
            .clickable(onClick = onConnectClick)
            .padding(
                start = PairedTvItemDimensions.contentStartPadding,
                top = PairedTvItemDimensions.contentTopPadding,
                bottom = PairedTvItemDimensions.contentBottomPadding,
                end = PairedTvItemDimensions.contentEndPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(PairedTvItemDimensions.tvIconContainerSize)
                .background(
                    if (isActive) {
                        AppColors.accentMuted
                    } else {
                        AppColors.surfacePressed
                    },
                    CircleShape
                )
                .border(
                    width = PairedTvItemDimensions.tvIconContainerBorderWidth,
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
                modifier = Modifier.size(PairedTvItemDimensions.tvIconSize)
            )
        }

        Spacer(modifier = Modifier.width(PairedTvItemDimensions.tvTextSpacing))

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
                    modifier = Modifier.padding(top = PairedTvItemDimensions.statusTopPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .size(PairedTvItemDimensions.statusDotSize)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(Modifier.width(PairedTvItemDimensions.statusTextSpacing))
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
                    .size(PairedTvItemDimensions.actionContainerSize)
                    .background(
                        AppColors.accentMuted.copy(alpha = 0.7f),
                        RoundedCornerShape(PairedTvItemDimensions.actionContainerCornerRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Renommer $displayName",
                    tint = AppColors.accent,
                    modifier = Modifier.size(PairedTvItemDimensions.renameIconSize)
                )
            }
        }

        IconButton(onClick = onForgetClick) {
            Box(
                modifier = Modifier
                    .size(PairedTvItemDimensions.actionContainerSize)
                    .background(
                        AppColors.statusRed.copy(alpha = 0.1f),
                        RoundedCornerShape(PairedTvItemDimensions.actionContainerCornerRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Oublier $displayName",
                    tint = AppColors.statusRed.copy(alpha = 0.9f),
                    modifier = Modifier.size(PairedTvItemDimensions.forgetIconSize)
                )
            }
        }
    }
}
