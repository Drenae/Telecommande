package com.telecommande.utils

import android.graphics.Paint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.outerShadow(
    color: Color = Color.Black,
    alpha: Float = 0.85f,
    blurRadius: Dp = 6.dp,
    offsetY: Dp = 3.dp
): Modifier = this.then(
    Modifier.drawBehind {
        val shadowColor = color.copy(alpha = alpha).toArgb()
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = shadowColor
            setShadowLayer(
                blurRadius.toPx(),
                0f,
                offsetY.toPx(),
                shadowColor
            )
        }

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawCircle(
                center.x,
                center.y,
                size.minDimension / 2f,
                paint
            )
        }
    }
)
