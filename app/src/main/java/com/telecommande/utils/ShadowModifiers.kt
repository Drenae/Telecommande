package com.telecommande.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asFrameworkPaint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
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
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        val shadowColor = color.copy(alpha = alpha).toArgb()

        frameworkPaint.color = shadowColor
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,
            offsetY.toPx(),
            shadowColor
        )

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawCircle(
                center.x,
                center.y,
                size.minDimension / 2f,
                frameworkPaint
            )
        }
    }
)
