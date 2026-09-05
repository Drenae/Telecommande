package com.telecommande.ui.home.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.HomeButtonShapes
import com.telecommande.ui.theme.LegacyHomeAppButtonDimensions
import com.telecommande.ui.theme.LegacyHomeBaseButtonDimensions
import com.telecommande.ui.theme.LegacyHomeDpadButtonDimensions

@Composable
fun HomeBaseButton(
    onClick: () -> Unit,
    vectorIcon: ImageVector? = null,
    drawableIconRes: Int? = null,
    textLabel: String? = null,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = LegacyHomeBaseButtonDimensions.defaultSize,
    defaultBackgroundColorBrush: Brush = DefaultButtonColors.defaultBackgroundBrush,
    pressedBackgroundColorBrush: Brush = DefaultButtonColors.pressedBackgroundBrush,
    shape: Shape = HomeButtonShapes.baseButton,
    iconTint: Color = AppColors.legacyHomeBaseButtonIcon,
    iconPadding: Dp = LegacyHomeBaseButtonDimensions.defaultIconPadding,
    borderWidth: Dp = LegacyHomeBaseButtonDimensions.defaultBorderWidth,
    borderColor: Color = DefaultButtonColors.defaultBorder,
    defaultElevation: Dp = LegacyHomeBaseButtonDimensions.defaultElevation,
    pressedElevation: Dp = LegacyHomeBaseButtonDimensions.pressedElevation,
    shadowColorDark: Color = DefaultButtonColors.defaultShadowDark,
    pressedShadowColorDark: Color = DefaultButtonColors.pressedShadowDark
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .92f else 1f,
        label = "remoteScale"
    )
    val brush = if (pressed) pressedBackgroundColorBrush else defaultBackgroundColorBrush
    val elevation = if (pressed) pressedElevation else defaultElevation
    val shadow = if (pressed) pressedShadowColorDark else shadowColorDark

    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        interactionSource = source
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    spotColor = shadow,
                    ambientColor = shadow
                )
                .background(brush, shape)
                .border(borderWidth, borderColor, shape),
            contentAlignment = Alignment.Center
        ) {
            if (textLabel != null) {
                Text(
                    text = textLabel,
                    color = if (iconTint == Color.Unspecified) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        iconTint
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            } else {
                vectorIcon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = contentDescription,
                        tint = iconTint,
                        modifier = Modifier.size(size - iconPadding * 2)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDpadButton(
    onClick: () -> Unit,
    vectorIcon: ImageVector? = null,
    drawableIconRes: Int? = null,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = LegacyHomeDpadButtonDimensions.defaultSize,
    iconTint: Color = AppColors.legacyHomeDpadButtonIcon,
    iconPadding: Dp
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .82f else 1f,
        label = "dpadScale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        interactionSource = source
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            vectorIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(size - iconPadding * 2)
                )
            }
        }
    }
}

@Composable
fun HomeAppButton(
    onClick: () -> Unit,
    vectorIcon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    shape: Shape = HomeButtonShapes.appButton,
    defaultBackgroundBrush: Brush = DefaultButtonColors.defaultBackgroundBrush,
    pressedBackgroundBrush: Brush = DefaultButtonColors.pressedBackgroundBrush,
    iconTint: Color = AppColors.legacyHomeAppButtonIcon,
    iconSize: Dp = LegacyHomeAppButtonDimensions.defaultIconSize
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) .96f else 1f,
        label = "appScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (pressed) {
                    LegacyHomeBaseButtonDimensions.pressedElevation
                } else {
                    LegacyHomeBaseButtonDimensions.defaultElevation
                },
                shape = shape
            )
            .background(
                brush = if (pressed) pressedBackgroundBrush else defaultBackgroundBrush,
                shape = shape
            )
            .border(
                width = LegacyHomeBaseButtonDimensions.defaultBorderWidth,
                color = DefaultButtonColors.defaultBorder,
                shape = shape
            )
            .clickable(
                onClick = onClick,
                interactionSource = source,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = vectorIcon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
