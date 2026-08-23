package com.telecommande.ui.home.config

import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
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
import com.telecommande.R
import com.telecommande.ui.home.composables.HomeAppButton
import com.telecommande.ui.home.composables.HomeBaseButton
import com.telecommande.ui.home.composables.HomeDpadButton
import com.telecommande.ui.theme.AppColors
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
    val contentDescription: String,
    val baseType: HomeButtonType = HomeButtonType.BASE_REMOTE,
    val vectorIcon: ImageVector? = null,
    @DrawableRes val drawableIconRes: Int? = null,
    val size: Dp? = null,
    val iconTint: Color = AppColors.appWhite,
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
    val Power = remoteButton(Icons.Rounded.PowerSettingsNew, "Power", tint = AppColors.accent)
    val Back = remoteButton(Icons.Rounded.ArrowBack, "Retour")
    val Home = remoteButton(Icons.Rounded.Home, "Accueil")

    val Ok = remoteButton(
        icon = Icons.Rounded.Check,
        description = "OK",
        size = AppSpecificButtonSizes.OkButtonSize,
        tint = AppColors.accent
    )
    val Up = dpadButton(Icons.Rounded.KeyboardArrowUp, "Haut")
    val Down = dpadButton(Icons.Rounded.KeyboardArrowDown, "Bas")
    val Left = dpadButton(Icons.Rounded.KeyboardArrowLeft, "Gauche")
    val Right = dpadButton(Icons.Rounded.KeyboardArrowRight, "Droite")

    val Netflix = appButton(R.drawable.ic_app_netflix, "Lancer Netflix", 80.dp)
    val YouTube = appButton(R.drawable.ic_app_youtube, "Lancer YouTube", 100.dp)
    val Plex = appButton(R.drawable.ic_app_plex, "Lancer Plex", 60.dp)
    val Crunchyroll = appButton(R.drawable.ic_app_crunchy, "Lancer Crunchyroll", 120.dp)

    private fun remoteButton(
        icon: ImageVector,
        description: String,
        size: Dp? = null,
        tint: Color = AppColors.appWhite
    ) = HomeButtonConfig(
        vectorIcon = icon,
        contentDescription = description,
        size = size,
        iconTint = tint
    )

    private fun dpadButton(icon: ImageVector, description: String) = HomeButtonConfig(
        vectorIcon = icon,
        contentDescription = description,
        baseType = HomeButtonType.DPAD_REMOTE,
        iconTint = AppColors.accent
    )

    private fun appButton(@DrawableRes iconRes: Int, description: String, iconSize: Dp) = HomeButtonConfig(
        drawableIconRes = iconRes,
        contentDescription = description,
        baseType = HomeButtonType.APP_LAUNCHER,
        iconTint = Color.Unspecified,
        shape = RoundedCornerShape(14.dp),
        defaultBackgroundColorBrush = DefaultButtonColors.DefaultBackgroundBrush,
        pressedBackgroundColorBrush = DefaultButtonColors.PressedBackgroundBrush,
        appLauncherIconSize = iconSize
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
            icon = requireNotNull(config.vectorIcon),
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
            shadowColorDark = config.shadowColorDark ?: DefaultButtonColors.DefaultShadowDark,
            pressedShadowColorDark = config.pressedShadowColorDark ?: DefaultButtonColors.PressedShadowDark
        )

        HomeButtonType.DPAD_REMOTE -> HomeDpadButton(
            onClick = onClick,
            icon = requireNotNull(config.vectorIcon),
            contentDescription = config.contentDescription,
            modifier = modifier,
            size = config.size ?: HomeDpadButtonSpecs.DefaultSize,
            iconTint = config.iconTint,
            iconPadding = config.iconPadding ?: 0.dp
        )

        HomeButtonType.APP_LAUNCHER -> HomeAppButton(
            onClick = onClick,
            iconRes = requireNotNull(config.drawableIconRes),
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
