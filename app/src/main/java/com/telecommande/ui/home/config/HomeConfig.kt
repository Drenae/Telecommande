package com.telecommande.ui.home.config

import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.telecommande.R
import com.telecommande.ui.home.composables.HomeAppButton
import com.telecommande.ui.home.composables.HomeBaseButton
import com.telecommande.ui.home.composables.HomeDpadButton
import com.telecommande.ui.theme.AppSpecificButtonSizes
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.HomeAppButtonSpecs
import com.telecommande.ui.theme.HomeBaseButtonSpecs
import com.telecommande.ui.theme.HomeDpadButtonSpecs

enum class HomeButtonType {
    BASE_REMOTE,
    DPAD_REMOTE,
    APP_LAUNCHER
}

data class HomeButtonConfig(
    @DrawableRes val iconRes: Int,
    val contentDescription: String,
    val baseType: HomeButtonType = HomeButtonType.BASE_REMOTE,
    val size: Dp? = null,
    val iconTint: Color = Color.Unspecified,
    val iconPadding: Dp? = null,
    val appLauncherIconSize: Dp? = null,
    val defaultBackgroundColorBrush: Brush? = null,
    val pressedBackgroundColorBrush: Brush? = null,
    val shape: Shape? = null,
    val borderWidth: Dp? = null,
    val borderColor: Color? = null,
    val defaultElevation: Dp? = null,
    val pressedElevation: Dp? = null,
    val shadowColorLight: Color? = null,
    val shadowColorDark: Color? = null,
    val pressedShadowColorLight: Color? = null,
    val pressedShadowColorDark: Color? = null
)

object HomeButtons {
    val Power = HomeButtonConfig(R.drawable.ic_power, "Power")
    val Back = HomeButtonConfig(R.drawable.ic_back, "Retour")
    val Home = HomeButtonConfig(R.drawable.ic_home, "Accueil")
    val Keyboard = HomeButtonConfig(R.drawable.ic_keyboard, "Keyboard")
    val Mute = HomeButtonConfig(R.drawable.ic_mute, "Mute")

    val Ok = HomeButtonConfig(iconRes = R.drawable.ic_dpad_ok, contentDescription = "OK", size = AppSpecificButtonSizes.OkButtonSize)
    val Up = HomeButtonConfig(R.drawable.ic_dpad_up, "Up", baseType = HomeButtonType.DPAD_REMOTE)
    val Down = HomeButtonConfig(R.drawable.ic_dpad_down, "Down", baseType = HomeButtonType.DPAD_REMOTE)
    val Left = HomeButtonConfig(R.drawable.ic_dpad_left, "Left", baseType = HomeButtonType.DPAD_REMOTE)
    val Right = HomeButtonConfig(R.drawable.ic_dpad_right, "Right", baseType = HomeButtonType.DPAD_REMOTE)

    val Netflix = HomeButtonConfig(
        iconRes = R.drawable.ic_app_netflix,
        contentDescription = "Lancer Netflix",
        baseType = HomeButtonType.APP_LAUNCHER,
        iconTint = Color.Unspecified,
        shape = RoundedCornerShape(10.dp),
        defaultBackgroundColorBrush = DefaultButtonColors.DefaultBackgroundBrush,
        pressedBackgroundColorBrush = DefaultButtonColors.PressedBackgroundBrush,
        appLauncherIconSize = 80.dp
    )
    val YouTube = HomeButtonConfig(
        iconRes = R.drawable.ic_app_youtube,
        contentDescription = "Lancer YouTube",
        baseType = HomeButtonType.APP_LAUNCHER,
        iconTint = Color.Unspecified,
        shape = RoundedCornerShape(10.dp),
        defaultBackgroundColorBrush = DefaultButtonColors.DefaultBackgroundBrush,
        pressedBackgroundColorBrush = DefaultButtonColors.PressedBackgroundBrush,
        appLauncherIconSize = 100.dp
    )
    val Plex = HomeButtonConfig(
        iconRes = R.drawable.ic_app_plex,
        contentDescription = "Lancer Plex",
        baseType = HomeButtonType.APP_LAUNCHER,
        iconTint = Color.Unspecified,
        shape = RoundedCornerShape(10.dp),
        defaultBackgroundColorBrush = DefaultButtonColors.DefaultBackgroundBrush,
        pressedBackgroundColorBrush = DefaultButtonColors.PressedBackgroundBrush,
        appLauncherIconSize = 60.dp
    )
    val Crunchyroll = HomeButtonConfig(
        iconRes = R.drawable.ic_app_crunchy,
        contentDescription = "Lancer Crunchyroll",
        baseType = HomeButtonType.APP_LAUNCHER,
        iconTint = Color.Unspecified,
        shape = RoundedCornerShape(10.dp),
        defaultBackgroundColorBrush = DefaultButtonColors.DefaultBackgroundBrush,
        pressedBackgroundColorBrush = DefaultButtonColors.PressedBackgroundBrush,
        appLauncherIconSize = 120.dp
    )
}

@Composable
fun ConfigurableHomeButton(
    config: HomeButtonConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (config.baseType) {
        HomeButtonType.BASE_REMOTE -> HomeBaseButton(
            onClick = onClick,
            iconRes = config.iconRes,
            contentDescription = config.contentDescription,
            modifier = modifier,
            size = config.size ?: HomeBaseButtonSpecs.DefaultSize,
            defaultBackgroundColorBrush = config.defaultBackgroundColorBrush ?: DefaultButtonColors.DefaultBackgroundBrush,
            pressedBackgroundColorBrush = config.pressedBackgroundColorBrush ?: DefaultButtonColors.PressedBackgroundBrush,
            shape = config.shape ?: HomeBaseButtonSpecs.DefaultShape,
            iconTint = config.iconTint,
            iconPadding = config.iconPadding ?: HomeBaseButtonSpecs.DefaultIconPadding,
            borderWidth = config.borderWidth ?: HomeBaseButtonSpecs.DefaultBorderWidth,
            borderColor = config.borderColor ?: DefaultButtonColors.DefaultBorder,
            defaultElevation = config.defaultElevation ?: HomeBaseButtonSpecs.DefaultElevation,
            pressedElevation = config.pressedElevation ?: HomeBaseButtonSpecs.PressedElevation,
            shadowColorLight = config.shadowColorLight ?: DefaultButtonColors.DefaultShadowLight,
            shadowColorDark = config.shadowColorDark ?: DefaultButtonColors.DefaultShadowDark,
            pressedShadowColorLight = config.pressedShadowColorLight ?: DefaultButtonColors.PressedShadowLight,
            pressedShadowColorDark = config.pressedShadowColorDark ?: DefaultButtonColors.PressedShadowDark
        )
        HomeButtonType.DPAD_REMOTE -> HomeDpadButton(
            onClick = onClick,
            iconRes = config.iconRes,
            contentDescription = config.contentDescription,
            modifier = modifier,
            size = config.size ?: HomeDpadButtonSpecs.DefaultSize,
            iconTint = config.iconTint,
            iconPadding = config.iconPadding ?: 0.dp
        )
        HomeButtonType.APP_LAUNCHER -> HomeAppButton(
            onClick = onClick,
            iconRes = config.iconRes,
            contentDescription = config.contentDescription,
            modifier = modifier,
            shape = config.shape ?: HomeAppButtonSpecs.DefaultShape,
            defaultBackgroundBrush = config.defaultBackgroundColorBrush ?: HomeAppButtonSpecs.DefaultBackgroundBrush,
            pressedBackgroundBrush = config.pressedBackgroundColorBrush ?: HomeAppButtonSpecs.PressedBackgroundBrush,
            iconTint = config.iconTint,
            iconSize = config.appLauncherIconSize ?: HomeAppButtonSpecs.DefaultIconSize
        )
    }
}