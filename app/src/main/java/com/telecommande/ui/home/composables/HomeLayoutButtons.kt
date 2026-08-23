package com.telecommande.ui.home.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.telecommande.ui.theme.ComponentDimensions
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.HomeAppButtonSpecs
import com.telecommande.ui.theme.HomeBaseButtonSpecs
import com.telecommande.ui.theme.HomeDpadButtonSpecs

@Composable
fun HomeBaseButton(
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = HomeBaseButtonSpecs.DefaultSize,
    defaultBackgroundColorBrush: Brush = DefaultButtonColors.DefaultBackgroundBrush,
    pressedBackgroundColorBrush: Brush = DefaultButtonColors.PressedBackgroundBrush,
    shape: Shape = HomeBaseButtonSpecs.DefaultShape,
    iconTint: Color = Color.Unspecified,
    iconPadding: Dp = HomeBaseButtonSpecs.DefaultIconPadding,
    borderWidth: Dp = HomeBaseButtonSpecs.DefaultBorderWidth,
    borderColor: Color = DefaultButtonColors.DefaultBorder,
    defaultElevation: Dp = HomeBaseButtonSpecs.DefaultElevation,
    pressedElevation: Dp = HomeBaseButtonSpecs.PressedElevation,
    shadowColorDark: Color = DefaultButtonColors.DefaultShadowDark,
    pressedShadowColorDark: Color = DefaultButtonColors.PressedShadowDark
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentElevation = if (isPressed) pressedElevation else defaultElevation
    val currentShadowColor = if (isPressed) pressedShadowColorDark else shadowColorDark
    val currentBackgroundBrush = if (isPressed) pressedBackgroundColorBrush else defaultBackgroundColorBrush
    val iconSize = size - (iconPadding * 2)
    val pressedIconSize = iconSize - HomeBaseButtonSpecs.PressedIconReduction

    IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        interactionSource = interactionSource
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = currentElevation,
                    shape = shape,
                    spotColor = currentShadowColor,
                    ambientColor = currentShadowColor
                )
                .background(brush = currentBackgroundBrush, shape = shape)
                .border(width = borderWidth, color = borderColor, shape = shape)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(if (isPressed) pressedIconSize else iconSize)
            )
        }
    }
}

@Composable
fun HomeDpadButton(
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = HomeDpadButtonSpecs.DefaultSize,
    iconTint: Color = Color.Unspecified,
    iconPadding: Dp = 0.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(size - (iconPadding * 2))
        )
    }
}

@Composable
fun HomeAppButton(
    onClick: () -> Unit,
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    shape: Shape,
    defaultBackgroundBrush: Brush = HomeAppButtonSpecs.DefaultBackgroundBrush,
    pressedBackgroundBrush: Brush = HomeAppButtonSpecs.PressedBackgroundBrush,
    iconTint: Color = Color.Unspecified,
    iconSize: Dp = ComponentDimensions.AppLauncherIconSize
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val currentBackgroundBrush = if (isPressed) pressedBackgroundBrush else defaultBackgroundBrush

    Box(
        modifier = modifier
            .background(brush = currentBackgroundBrush, shape = shape)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}