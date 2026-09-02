package com.telecommande.ui.home.config

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.telecommande.ui.home.composables.HomeAppButton
import com.telecommande.ui.home.composables.HomeBaseButton
import com.telecommande.ui.home.composables.HomeDpadButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.AppSpecificButtonSizes
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.HomeAppButtonSpecs
import com.telecommande.ui.theme.HomeBaseButtonSpecs
import com.telecommande.ui.theme.HomeDpadButtonSpecs

enum class HomeButtonType { BASE_REMOTE, DPAD_REMOTE, APP_LAUNCHER }

data class HomeButtonConfig(
    val contentDescription: String,
    val baseType: HomeButtonType = HomeButtonType.BASE_REMOTE,
    val vectorIcon: ImageVector? = null,
    val textLabel: String? = null,
    val size: Dp? = null,
    val iconTint: Color = Color.White,
    val iconPadding: Dp? = null,
    val appLauncherIconSize: Dp? = null,
    val defaultBackgroundColorBrush: Brush? = null,
    val pressedBackgroundColorBrush: Brush? = null,
    val shape: Shape? = null,
    val borderWidth: Dp? = null,
    val borderColor: Color? = null,
    val defaultElevation: Dp? = null,
    val pressedElevation: Dp? = null,
    val shadowColorDark: Color? = null,
    val pressedShadowColorDark: Color? = null
)

object HomeButtons {
    val Power = remote(Icons.Rounded.PowerSettingsNew, "Power")
    val Back = remote(Icons.AutoMirrored.Rounded.ArrowBack, "Retour")
    val Home = remote(Icons.Rounded.Home, "Accueil")
    val Ok = HomeButtonConfig("OK", textLabel = "OK", size = AppSpecificButtonSizes.OkButtonSize, iconTint = AppColors.accent, shape = CircleShape)
    val Up = dpad(Icons.Rounded.KeyboardArrowUp, "Haut")
    val Down = dpad(Icons.Rounded.KeyboardArrowDown, "Bas")
    val Left = dpad(Icons.Rounded.KeyboardArrowLeft, "Gauche")
    val Right = dpad(Icons.Rounded.KeyboardArrowRight, "Droite")

    // Material-only launcher artwork: no PNG/SVG application logos.
    val Netflix = app("Lancer Netflix")
    val YouTube = app("Lancer YouTube")
    val Plex = app("Lancer Plex")
    val Crunchyroll = app("Lancer Crunchyroll")

    private fun remote(icon: ImageVector, description: String) = HomeButtonConfig(vectorIcon = icon, contentDescription = description)
    private fun dpad(icon: ImageVector, description: String) = HomeButtonConfig(vectorIcon = icon, contentDescription = description, baseType = HomeButtonType.DPAD_REMOTE, iconTint = Color.White)
    private fun app(description: String) = HomeButtonConfig(vectorIcon = Icons.Rounded.Apps, contentDescription = description, baseType = HomeButtonType.APP_LAUNCHER, iconTint = Color.White, shape = RoundedCornerShape(18.dp), defaultBackgroundColorBrush = DefaultButtonColors.DefaultBackgroundBrush, pressedBackgroundColorBrush = DefaultButtonColors.PressedBackgroundBrush, appLauncherIconSize = 34.dp)
}

@Composable
fun ConfigurableHomeButton(config: HomeButtonConfig, onClick: () -> Unit, modifier: Modifier = Modifier) {
    when (config.baseType) {
        HomeButtonType.BASE_REMOTE -> HomeBaseButton(onClick, config.vectorIcon, null, config.textLabel, config.contentDescription, modifier, config.size ?: HomeBaseButtonSpecs.DefaultSize, config.defaultBackgroundColorBrush ?: DefaultButtonColors.DefaultBackgroundBrush, config.pressedBackgroundColorBrush ?: DefaultButtonColors.PressedBackgroundBrush, config.shape ?: HomeBaseButtonSpecs.DefaultShape, config.iconTint, config.iconPadding ?: HomeBaseButtonSpecs.DefaultIconPadding, config.borderWidth ?: HomeBaseButtonSpecs.DefaultBorderWidth, config.borderColor ?: DefaultButtonColors.DefaultBorder, config.defaultElevation ?: HomeBaseButtonSpecs.DefaultElevation, config.pressedElevation ?: HomeBaseButtonSpecs.PressedElevation, config.shadowColorDark ?: DefaultButtonColors.DefaultShadowDark, config.pressedShadowColorDark ?: DefaultButtonColors.PressedShadowDark)
        HomeButtonType.DPAD_REMOTE -> HomeDpadButton(onClick, config.vectorIcon, null, config.contentDescription, modifier, config.size ?: HomeDpadButtonSpecs.DefaultSize, config.iconTint, config.iconPadding ?: 0.dp)
        HomeButtonType.APP_LAUNCHER -> HomeAppButton(onClick, requireNotNull(config.vectorIcon), config.contentDescription, modifier, config.shape ?: HomeAppButtonSpecs.DefaultShape, config.defaultBackgroundColorBrush ?: HomeAppButtonSpecs.DefaultBackgroundBrush, config.pressedBackgroundColorBrush ?: HomeAppButtonSpecs.PressedBackgroundBrush, config.iconTint, config.appLauncherIconSize ?: HomeAppButtonSpecs.DefaultIconSize)
    }
}
