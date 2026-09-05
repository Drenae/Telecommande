package com.telecommande.util

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import com.telecommande.ui.theme.ShadowModifierDimensions

fun Modifier.outerShadow(
    color: Color = Color.Black,
    alpha: Float = 0.85f,
    blurRadius: Dp = ShadowModifierDimensions.defaultBlurRadius,
    offsetY: Dp = ShadowModifierDimensions.defaultOffsetY
): Modifier = this.then(
    Modifier.drawBehind {
        val shadowColor = color.copy(alpha = alpha).toArgb()
        val paint = Paint().apply {
            this.color = shadowColor
            setShadowLayer(
                blurRadius.toPx(),
                0f,
                offsetY.toPx(),
                shadowColor
            )
        }

        drawContext.canvas.nativeCanvas.drawCircle(
            center.x,
            center.y,
            size.minDimension / 2f,
            paint
        )
    }
)

fun Modifier.outerRoundedShadow(
    cornerRadius: Dp,
    color: Color = Color.Black,
    alpha: Float = 0.85f,
    blurRadius: Dp = ShadowModifierDimensions.defaultBlurRadius,
    offsetY: Dp = ShadowModifierDimensions.defaultOffsetY
): Modifier = this.then(
    Modifier.drawBehind {
        val shadowColor = color.copy(alpha = alpha).toArgb()
        val paint = Paint().apply {
            this.color = shadowColor
            setShadowLayer(
                blurRadius.toPx(),
                0f,
                offsetY.toPx(),
                shadowColor
            )
        }
        val radiusPx = cornerRadius.toPx()

        drawContext.canvas.nativeCanvas.drawRoundRect(
            RectF(0f, 0f, size.width, size.height),
            radiusPx,
            radiusPx,
            paint
        )
    }
)
