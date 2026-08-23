package com.telecommande.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DefaultButtonColors {
    val DefaultBackgroundStart = Color(0xFF2C2C2E)
    val DefaultBackgroundEnd = Color(0xFF161616)
    val PressedBackgroundStart = Color(0xFF1F1F1F)
    val PressedBackgroundEnd = Color(0xFF282828)
    val DefaultBorder = Color(0xFF161616)

    val DefaultShadowLight = Color(0x66FFFFFF)
    val DefaultShadowDark = Color(0x66000000)
    val PressedShadowLight = Color(0x66000000)
    val PressedShadowDark = Color(0x33FFFFFF)

    val DefaultBackgroundBrush = Brush.linearGradient(colors = listOf(DefaultBackgroundStart, DefaultBackgroundEnd))
    val PressedBackgroundBrush = Brush.linearGradient(colors = listOf(PressedBackgroundStart, PressedBackgroundEnd))
}

object AppSliderColors {
    val thumbColor = Color.White
    val activeTrackColor = DefaultButtonColors.DefaultBackgroundStart
    val inactiveTrackColor = DefaultButtonColors.DefaultBackgroundEnd.copy(alpha = 0.4f)
    val activeTickColor = Color.Transparent
    val inactiveTickColor = Color.Transparent
}

object HomeBaseButtonSpecs {
    val DefaultSize: Dp = 74.dp
    val DefaultIconPadding: Dp = 16.dp
    val PressedIconReduction: Dp = 2.dp
    val DefaultBorderWidth: Dp = 1.dp
    val DefaultElevation: Dp = 8.dp
    val PressedElevation: Dp = 2.dp
    val DefaultShape: Shape = CircleShape
}

object HomeDpadButtonSpecs {
    val DefaultSize: Dp = 50.dp
}

object HomeAppButtonSpecs {
    val DefaultShape: Shape = RoundedCornerShape(8.dp)
    val DefaultBackgroundBrush: Brush = DefaultButtonColors.DefaultBackgroundBrush
    val PressedBackgroundBrush: Brush = DefaultButtonColors.PressedBackgroundBrush
    val DefaultIconSize: Dp = ComponentDimensions.AppLauncherIconSize
}

object AppSpecificButtonSizes {
    val OkButtonSize: Dp = 120.dp
}

object DpadSectionSpecs {
    val ContainerSize: Dp = 280.dp
    val ContainerShape: Shape = CircleShape
    val BorderWidth: Dp = 1.dp
    val BorderColor: Color = DefaultButtonColors.DefaultBorder
    val BackgroundBrush: Brush = DefaultButtonColors.DefaultBackgroundBrush
    val ArrowButtonMargin: Dp = 1.dp
}

object ScreenPaddings {
    val Horizontal: Dp = 20.dp
}

object ComponentDimensions {
    val StatusIndicatorSize: Dp = 44.dp

    val NavButtonOffsetNegativeY: Dp = (-32).dp

    val MainControlsInternalPaddingHorizontal: Dp = 8.dp
    val MainControlsVerticalButtonMarginEnd: Dp = 8.dp

    val DefaultSpacerHeight: Dp = 50.dp
    val MediumSpacerHeight: Dp = 16.dp
    val LargeSpacerHeight: Dp = 50.dp

    val FooterPaddingVertical: Dp = 16.dp
    val AppLauncherGridSpacing: Dp = 8.dp
    val AppLauncherIconSize: Dp = 32.dp
    val AppLauncherButtonHeight: Dp = 60.dp
}

object TvManagementSpecs {
    val ListItemVerticalPadding: Dp = 4.dp
    val ListItemHorizontalPadding: Dp = 8.dp
    val ListItemInternalPadding: Dp = 16.dp
    val ListItemSpacerWidth: Dp = 16.dp
    val PairedTvStatusIconSize: Dp = 24.dp
    val PairedTvForgetButtonSpacer: Dp = 8.dp

    val PinDialogVerticalSpacer: Dp = 16.dp
    val PinDialogOverallPadding: Dp = 16.dp

    val ScreenSectionTitlePadding: Dp = 16.dp
    val LazyColumnHorizontalPadding: Dp = 8.dp
    val DividerVerticalPadding: Dp = 8.dp
    val EmptyStateIconBottomPadding: Dp = 8.dp
    val LoadingScreenSpacerHeight: Dp = 8.dp
    val DisconnectButtonPadding: Dp = 16.dp
}