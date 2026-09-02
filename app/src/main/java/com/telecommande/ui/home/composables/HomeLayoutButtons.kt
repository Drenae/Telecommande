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
import com.telecommande.ui.theme.ComponentDimensions
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.HomeAppButtonSpecs
import com.telecommande.ui.theme.HomeBaseButtonSpecs
import com.telecommande.ui.theme.HomeDpadButtonSpecs

@Composable
fun HomeBaseButton(onClick: () -> Unit, vectorIcon: ImageVector? = null, drawableIconRes: Int? = null, textLabel: String? = null, contentDescription: String, modifier: Modifier = Modifier, size: Dp = HomeBaseButtonSpecs.DefaultSize, defaultBackgroundColorBrush: Brush = DefaultButtonColors.DefaultBackgroundBrush, pressedBackgroundColorBrush: Brush = DefaultButtonColors.PressedBackgroundBrush, shape: Shape = HomeBaseButtonSpecs.DefaultShape, iconTint: Color = Color.White, iconPadding: Dp = HomeBaseButtonSpecs.DefaultIconPadding, borderWidth: Dp = HomeBaseButtonSpecs.DefaultBorderWidth, borderColor: Color = DefaultButtonColors.DefaultBorder, defaultElevation: Dp = HomeBaseButtonSpecs.DefaultElevation, pressedElevation: Dp = HomeBaseButtonSpecs.PressedElevation, shadowColorDark: Color = DefaultButtonColors.DefaultShadowDark, pressedShadowColorDark: Color = DefaultButtonColors.PressedShadowDark) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .92f else 1f, label = "remoteScale")
    val brush = if (pressed) pressedBackgroundColorBrush else defaultBackgroundColorBrush
    val elevation = if (pressed) pressedElevation else defaultElevation
    val shadow = if (pressed) pressedShadowColorDark else shadowColorDark
    IconButton(onClick = onClick, modifier = modifier.size(size), interactionSource = source) {
        Box(Modifier.size(size).scale(scale).shadow(elevation, shape, spotColor = shadow, ambientColor = shadow).background(brush, shape).border(borderWidth, borderColor, shape), contentAlignment = Alignment.Center) {
            if (textLabel != null) Text(textLabel, color = if (iconTint == Color.Unspecified) MaterialTheme.colorScheme.primary else iconTint, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            else vectorIcon?.let { Icon(it, contentDescription, tint = iconTint, modifier = Modifier.size(size - iconPadding * 2)) }
        }
    }
}

@Composable
fun HomeDpadButton(onClick: () -> Unit, vectorIcon: ImageVector? = null, drawableIconRes: Int? = null, contentDescription: String, modifier: Modifier = Modifier, size: Dp = HomeDpadButtonSpecs.DefaultSize, iconTint: Color = Color.White, iconPadding: Dp) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .82f else 1f, label = "dpadScale")
    IconButton(onClick = onClick, modifier = modifier.size(size), interactionSource = source) {
        Box(Modifier.size(size).scale(scale), contentAlignment = Alignment.Center) { vectorIcon?.let { Icon(it, contentDescription, tint = iconTint, modifier = Modifier.size(size - iconPadding * 2)) } }
    }
}

@Composable
fun HomeAppButton(onClick: () -> Unit, vectorIcon: ImageVector, contentDescription: String, modifier: Modifier = Modifier, shape: Shape, defaultBackgroundBrush: Brush = HomeAppButtonSpecs.DefaultBackgroundBrush, pressedBackgroundBrush: Brush = HomeAppButtonSpecs.PressedBackgroundBrush, iconTint: Color = Color.White, iconSize: Dp = ComponentDimensions.AppLauncherIconSize) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, label = "appScale")
    Box(modifier.scale(scale).shadow(if (pressed) HomeBaseButtonSpecs.PressedElevation else HomeBaseButtonSpecs.DefaultElevation, shape).background(if (pressed) pressedBackgroundBrush else defaultBackgroundBrush, shape).border(HomeBaseButtonSpecs.DefaultBorderWidth, DefaultButtonColors.DefaultBorder, shape).clickable(onClick = onClick, interactionSource = source, indication = null), contentAlignment = Alignment.Center) {
        Icon(vectorIcon, contentDescription, tint = iconTint, modifier = Modifier.size(iconSize))
    }
}
