package com.telecommande.ui.settings.controls

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
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.PairedTvItemDimensions
import com.telecommande.util.outerRoundedShadow
import com.telecommande.util.technicalName

@Composable
fun PairedTvControl(
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
        isConnectedToThisTv -> AppColors.pairedTvItemConnectedStatus
        isActive -> AppColors.pairedTvItemActiveStatus
        else -> AppColors.pairedTvItemInactiveStatus
    }
    val technicalName = tvInfo.technicalName()
    val shape = RoundedCornerShape(PairedTvItemDimensions.cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = PairedTvItemDimensions.cornerRadius,
                color = AppColors.pairedTvItemShadow,
                alpha = 0.58f,
                blurRadius = PairedTvItemDimensions.shadowBlurRadius,
                offsetY = PairedTvItemDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    if (isActive) {
                        listOf(
                            AppColors.pairedTvItemActiveGradientTop,
                            AppColors.pairedTvItemActiveGradientMiddle,
                            AppColors.pairedTvItemActiveGradientBottom
                        )
                    } else {
                        listOf(
                            AppColors.pairedTvItemInactiveGradientTop,
                            AppColors.pairedTvItemInactiveGradientMiddle,
                            AppColors.pairedTvItemInactiveGradientBottom
                        )
                    }
                ),
                shape
            )
            .border(
                width = PairedTvItemDimensions.mainBorderWidth,
                color = if (isActive) AppColors.pairedTvItemActiveBorder else AppColors.pairedTvItemInactiveBorder,
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
                    if (isActive) AppColors.pairedTvItemActiveIconBackground else AppColors.pairedTvItemInactiveIconBackground,
                    CircleShape
                )
                .border(
                    width = PairedTvItemDimensions.tvIconContainerBorderWidth,
                    color = if (isActive) AppColors.pairedTvItemActiveIconBorder else AppColors.pairedTvItemInactiveIconBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tv,
                contentDescription = null,
                tint = if (isActive) AppColors.pairedTvItemActiveIcon else AppColors.pairedTvItemInactiveIcon,
                modifier = Modifier.size(PairedTvItemDimensions.tvIconSize)
            )
        }

        Spacer(modifier = Modifier.width(PairedTvItemDimensions.tvTextSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.pairedTvItemTitle
            )

            if (displayName != technicalName) {
                Text(
                    text = technicalName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.pairedTvItemTechnicalName
                )
            }

            Text(
                text = tvInfo.ipAddress,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.pairedTvItemIpAddress
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
                        AppColors.pairedTvItemRenameBackground,
                        RoundedCornerShape(PairedTvItemDimensions.actionContainerCornerRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Renommer $displayName",
                    tint = AppColors.pairedTvItemRenameIcon,
                    modifier = Modifier.size(PairedTvItemDimensions.renameIconSize)
                )
            }
        }

        IconButton(onClick = onForgetClick) {
            Box(
                modifier = Modifier
                    .size(PairedTvItemDimensions.actionContainerSize)
                    .background(
                        AppColors.pairedTvItemForgetBackground,
                        RoundedCornerShape(PairedTvItemDimensions.actionContainerCornerRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Oublier $displayName",
                    tint = AppColors.pairedTvItemForgetIcon,
                    modifier = Modifier.size(PairedTvItemDimensions.forgetIconSize)
                )
            }
        }
    }
}
