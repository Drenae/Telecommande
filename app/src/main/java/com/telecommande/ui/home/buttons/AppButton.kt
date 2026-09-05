package com.telecommande.ui.home.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.AppTileDimensions
import com.telecommande.util.outerRoundedShadow

@Composable
fun AppButton(
    label: String,
    borderColor: Color,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTileDimensions.cornerRadius)

    Row(
        modifier
            .height(AppTileDimensions.height)
            .outerRoundedShadow(
                cornerRadius = AppTileDimensions.cornerRadius,
                color = AppColors.appTileShadow,
                alpha = 0.9f,
                blurRadius = AppTileDimensions.shadowBlurRadius,
                offsetY = AppTileDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.appTileGradientTop,
                        0.35f to AppColors.appTileGradientBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 500f)
                ),
                shape
            )
            .border(
                width = AppTileDimensions.mainBorderWidth,
                color = borderColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = AppTileDimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(AppTileDimensions.logoSize)
        )
    }
}
