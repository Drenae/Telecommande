package com.telecommande.ui.home.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.ui.home.buttons.CircleButton
import com.telecommande.ui.home.buttons.DpadButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.HomeDpadDimensions
import com.telecommande.util.outerShadow
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CaretDown
import compose.icons.fontawesomeicons.solid.CaretLeft
import compose.icons.fontawesomeicons.solid.CaretRight
import compose.icons.fontawesomeicons.solid.CaretUp

@Composable
fun DpadControl(
    size: Dp,
    onOkClick: () -> Unit,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier
            .size(size)
            .outerShadow(
                color = AppColors.homeDpadShadow,
                alpha = 0.9f,
                blurRadius = HomeDpadDimensions.shadowBlurRadius,
                offsetY = HomeDpadDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.homeDpadGradientTop,
                        0.42f to AppColors.homeDpadGradientMiddle,
                        1.0f to AppColors.homeDpadGradientBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 700f)
                ),
                CircleShape
            )
            .border(
                width = HomeDpadDimensions.mainBorderWidth,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.homeDpadBorderTop,
                        0.18f to AppColors.homeDpadBorderBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 700f)
                ),
                shape = CircleShape
            )
    ) {
        val (ok, up, down, left, right) = createRefs()

        CircleButton(
            icon = Icons.Rounded.Check,
            contentDescription = "OK",
            size = size * .31f,
            onClick = onOkClick,
            modifier = Modifier.constrainAs(ok) {
                centerTo(parent)
            },
            iconScale = .62f
        )

        DpadButton(
            icon = FontAwesomeIcons.Solid.CaretUp,
            contentDescription = "Haut",
            size = size * .40f,
            onClick = onUpClick,
            modifier = Modifier.constrainAs(up) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        DpadButton(
            icon = FontAwesomeIcons.Solid.CaretDown,
            contentDescription = "Bas",
            size = size * .40f,
            onClick = onDownClick,
            modifier = Modifier.constrainAs(down) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

        DpadButton(
            icon = FontAwesomeIcons.Solid.CaretLeft,
            contentDescription = "Gauche",
            size = size * .40f,
            onClick = onLeftClick,
            modifier = Modifier.constrainAs(left) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        DpadButton(
            icon = FontAwesomeIcons.Solid.CaretRight,
            contentDescription = "Droite",
            size = size * .40f,
            onClick = onRightClick,
            modifier = Modifier.constrainAs(right) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
    }
}
