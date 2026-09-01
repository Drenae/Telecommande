package com.telecommande.ui.home.composables

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.telecommande.ui.theme.ComponentDimensions
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.HomeAppButtonSpecs
import com.telecommande.ui.theme.HomeBaseButtonSpecs
import com.telecommande.ui.theme.HomeDpadButtonSpecs

@Composable
fun HomeBaseButton(
    onClick: () -> Unit,
    vectorIcon: ImageVector? = null,
    @DrawableRes drawableIconRes: Int? = null,
    textLabel: String? = null,
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
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "remoteButtonScale")
    val currentElevation = if (isPressed) pressedElevation else defaultElevation
    val currentShadowColor = if (isPressed) pressedShadowColorDark else shadowColorDark
    val currentBackgroundBrush = if (isPressed) pressedBackgroundColorBrush else defaultBackgroundColorBrush
    val iconSize = size - (iconPadding * 2)

    IconButton(onClick = onClick, modifier = modifier.size(size), interactionSource = interactionSource) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .scale(scale)
                .shadow(currentElevation, shape, spotColor = currentShadowColor, ambientColor = currentShadowColor)
                .background(currentBackgroundBrush, shape)
                .border(borderWidth, borderColor, shape)
        ) {
            if (textLabel != null) {
                Text(
                    text = textLabel,
                    color = if (iconTint == Color.Unspecified) MaterialTheme.colorScheme.primary else iconTint,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )
            } else {
                RemoteArtwork(vectorIcon, drawableIconRes, contentDescription, iconTint, iconSize)
            }
        }
    }
}

@Composable
fun HomeDpadButton(
    onClick: () -> Unit,
    vectorIcon: ImageVector? = null,
    @DrawableRes drawableIconRes: Int? = null,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = HomeDpadButtonSpecs.DefaultSize,
    iconTint: Color = Color.Unspecified,
    iconPadding: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.84f else 1f, label = "dpadButtonScale")
    IconButton(onClick = onClick, modifier = modifier.size(size), interactionSource = interactionSource) {
        Box(Modifier.size(size).scale(scale), contentAlignment = Alignment.Center) {
            RemoteArtwork(vectorIcon, drawableIconRes, contentDescription, iconTint, size - (iconPadding * 2))
        }
    }
}

@Composable
private fun RemoteArtwork(
    vectorIcon: ImageVector?,
    @DrawableRes drawableIconRes: Int?,
    contentDescription: String,
    tint: Color,
    size: Dp
) {
    when {
        drawableIconRes != null -> Icon(
            painter = painterResource(drawableIconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
        vectorIcon != null -> Icon(
            imageVector = vectorIcon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
        else -> error("Aucun artwork fourni pour $contentDescription")
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
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "appButtonScale")
    val currentBackgroundBrush = if (isPressed) pressedBackgroundBrush else defaultBackgroundBrush

    Box(
        modifier = modifier
            .scale(scale)
            .background(currentBackgroundBrush, shape)
            .border(HomeBaseButtonSpecs.DefaultBorderWidth, DefaultButtonColors.DefaultBorder, shape)
            .clickable(onClick = onClick, interactionSource = interactionSource, indication = null),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), contentDescription, tint = iconTint, modifier = Modifier.size(iconSize))
    }
}
