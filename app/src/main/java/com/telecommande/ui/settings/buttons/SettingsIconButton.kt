package com.telecommande.ui.settings.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.SettingsIconButtonDimensions
import com.telecommande.util.outerRoundedShadow

@Composable
fun SettingsIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SettingsIconButtonDimensions.cornerRadius)

    Box(
        modifier = modifier
            .padding(start = SettingsIconButtonDimensions.startPadding)
            .size(SettingsIconButtonDimensions.size)
            .outerRoundedShadow(
                cornerRadius = SettingsIconButtonDimensions.cornerRadius,
                color = AppColors.settingsIconButtonShadow,
                alpha = 0.72f,
                blurRadius = SettingsIconButtonDimensions.shadowBlurRadius,
                offsetY = SettingsIconButtonDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.settingsIconButtonGradientTop,
                        AppColors.settingsIconButtonGradientBottom
                    )
                ),
                shape
            )
            .border(
                width = SettingsIconButtonDimensions.mainBorderWidth,
                color = AppColors.settingsIconButtonBorder,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.settingsIconButtonIcon,
            modifier = Modifier.size(SettingsIconButtonDimensions.iconSize)
        )
    }
}
