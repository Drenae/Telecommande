package com.telecommande.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    onPrimary = AppColors.appBlack,
    primaryContainer = AppColors.accentMuted,
    onPrimaryContainer = AppColors.appWhite,
    background = AppColors.darkBackground,
    onBackground = AppColors.appWhite,
    surface = AppColors.surface,
    onSurface = AppColors.appWhite,
    surfaceVariant = AppColors.surfaceElevated,
    onSurfaceVariant = AppColors.textSecondary,
    outline = AppColors.border,
    error = AppColors.statusRed
)

@Composable
fun TelecommandeTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
