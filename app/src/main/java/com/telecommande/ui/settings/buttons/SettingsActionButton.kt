package com.telecommande.ui.settings.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.SettingsActionButtonDimensions
import com.telecommande.util.outerRoundedShadow

@Composable
fun SettingsActionButton(
    isSearching: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SettingsActionButtonDimensions.cornerRadius)

    Row(
        modifier = modifier
            .padding(end = SettingsActionButtonDimensions.endPadding)
            .height(SettingsActionButtonDimensions.height)
            .outerRoundedShadow(
                cornerRadius = SettingsActionButtonDimensions.cornerRadius,
                color = AppColors.settingsActionButtonShadow,
                alpha = 0.72f,
                blurRadius = SettingsActionButtonDimensions.shadowBlurRadius,
                offsetY = SettingsActionButtonDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.settingsActionButtonGradientTop,
                        AppColors.settingsActionButtonGradientBottom
                    )
                ),
                shape
            )
            .border(
                width = SettingsActionButtonDimensions.mainBorderWidth,
                color = if (isSearching) {
                    AppColors.settingsActionButtonSearchingBorder
                } else {
                    AppColors.settingsActionButtonIdleBorder
                },
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsActionButtonDimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SettingsActionButtonDimensions.contentSpacing)
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Rounded.StopCircle else Icons.Rounded.Search,
            contentDescription = null,
            tint = if (isSearching) {
                AppColors.settingsActionButtonSearchingIcon
            } else {
                AppColors.settingsActionButtonIdleIcon
            },
            modifier = Modifier.size(SettingsActionButtonDimensions.iconSize)
        )
        Text(
            text = if (isSearching) "ARRÊTER" else "RECHERCHER",
            color = AppColors.settingsActionButtonText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
