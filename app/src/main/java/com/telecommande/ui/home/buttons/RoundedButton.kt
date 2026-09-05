package com.telecommande.ui.home.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.NavPillDimensions
import com.telecommande.util.GradientIcon
import com.telecommande.util.outerRoundedShadow

@Composable
fun RoundedButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(NavPillDimensions.cornerRadius)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(NavPillDimensions.width)
                .height(NavPillDimensions.height)
                .outerRoundedShadow(
                    cornerRadius = NavPillDimensions.cornerRadius,
                    color = AppColors.navPillShadow,
                    alpha = 0.9f,
                    blurRadius = NavPillDimensions.shadowBlurRadius,
                    offsetY = NavPillDimensions.shadowOffsetY
                )
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to AppColors.navPillGradientTop,
                            0.35f to AppColors.navPillGradientBottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 500f)
                    ),
                    shape
                )
                .border(
                    width = NavPillDimensions.mainBorderWidth,
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to AppColors.navPillMainBorderTop,
                            0.15f to AppColors.navPillMainBorderBottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 500f)
                    ),
                    shape = shape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = NavPillDimensions.secondaryBorderWidth,
                        color = AppColors.navPillSecondaryBorder,
                        shape = shape
                    )
                    .clip(shape),
                contentAlignment = Alignment.Center
            ) {
                GradientIcon(
                    icon = icon,
                    contentDescription = label,
                    modifier = Modifier.size(NavPillDimensions.iconSize)
                )
            }
        }

        Spacer(Modifier.height(NavPillDimensions.labelTopSpacing))

        Text(
            text = label,
            color = AppColors.navPillLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
