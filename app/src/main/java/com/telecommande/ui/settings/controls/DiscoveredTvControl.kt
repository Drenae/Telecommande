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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.DiscoveredTvItemDimensions
import com.telecommande.util.outerRoundedShadow

@Composable
fun DiscoveredTvControl(
    tv: DiscoveredTv,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DiscoveredTvItemDimensions.cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = DiscoveredTvItemDimensions.cornerRadius,
                color = AppColors.discoveredTvItemShadow,
                alpha = 0.55f,
                blurRadius = DiscoveredTvItemDimensions.shadowBlurRadius,
                offsetY = DiscoveredTvItemDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.discoveredTvItemGradientTop,
                        AppColors.discoveredTvItemGradientMiddle,
                        AppColors.discoveredTvItemGradientBottom
                    )
                ),
                shape
            )
            .border(
                width = DiscoveredTvItemDimensions.mainBorderWidth,
                color = AppColors.discoveredTvItemBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(DiscoveredTvItemDimensions.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(DiscoveredTvItemDimensions.tvIconContainerSize)
                .background(AppColors.discoveredTvItemIconBackground, CircleShape)
                .border(
                    width = DiscoveredTvItemDimensions.tvIconContainerBorderWidth,
                    color = AppColors.discoveredTvItemIconBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tv,
                contentDescription = null,
                tint = AppColors.discoveredTvItemIcon,
                modifier = Modifier.size(DiscoveredTvItemDimensions.tvIconSize)
            )
        }

        Spacer(modifier = Modifier.width(DiscoveredTvItemDimensions.tvTextSpacing))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tv.friendlyName ?: "Appareil inconnu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.discoveredTvItemTitle
            )

            tv.ipAddress?.let { ip ->
                Text(
                    text = ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.discoveredTvItemSubtitle
                )
            }
        }

        Box(
            modifier = Modifier
                .size(DiscoveredTvItemDimensions.actionContainerSize)
                .background(
                    AppColors.discoveredTvItemActionBackground,
                    RoundedCornerShape(DiscoveredTvItemDimensions.actionContainerCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Appairer cette TV",
                tint = AppColors.discoveredTvItemActionIcon,
                modifier = Modifier.size(DiscoveredTvItemDimensions.actionIconSize)
            )
        }
    }
}
