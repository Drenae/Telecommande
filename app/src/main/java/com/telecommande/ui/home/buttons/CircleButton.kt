package com.telecommande.ui.home.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.PremiumCircleDimensions
import com.telecommande.util.GradientIcon
import com.telecommande.util.outerShadow

@Composable
fun CircleButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconScale: Float = .60f,
    iconTint: Color? = null
) {
    Box(
        modifier
            .size(size)
            .outerShadow(
                color = AppColors.premiumCircleShadow,
                alpha = 0.9f,
                blurRadius = PremiumCircleDimensions.shadowBlurRadius,
                offsetY = PremiumCircleDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.premiumCircleGradientTop,
                        0.35f to AppColors.premiumCircleGradientBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 500f)
                ),
                CircleShape
            )
            .border(
                width = PremiumCircleDimensions.mainBorderWidth,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.premiumCircleMainBorderTop,
                        0.15f to AppColors.premiumCircleMainBorderBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 500f)
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .border(
                    width = PremiumCircleDimensions.secondaryBorderWidth,
                    color = AppColors.premiumCircleSecondaryBorder,
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            GradientIcon(
                icon = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * iconScale),
                tint = iconTint
            )
        }
    }
}
