package com.telecommande.util

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.telecommande.ui.theme.AppColors

@Composable
fun GradientIcon(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    brush: Brush = Brush.verticalGradient(
        listOf(
            AppColors.homeDpadIconGradientTop,
            AppColors.homeDpadIconGradientBottom
        )
    )
) {
    if (tint != null) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier
        )
        return
    }

    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = AppColors.homeDpadIconMask,
        modifier = modifier
            .graphicsLayer(alpha = 0.99f)
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = brush,
                        blendMode = BlendMode.SrcAtop
                    )
                }
            }
    )
}
