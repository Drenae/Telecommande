package com.telecommande.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent, // Votre couleur d'accentuation
    background = AppColors.darkBackground,
    surface = AppColors.gradientStart, // Couleur de surface pour les cartes, les feuilles, etc.
    onPrimary = AppColors.appBlack,
    onBackground = AppColors.appWhite,
    onSurface = AppColors.appWhite
    // Vous pouvez définir d'autres couleurs (secondary, tertiary, error, etc.)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun TelecommandeTheme( // Renommez comme vous le souhaitez
    darkTheme: Boolean = isSystemInDarkTheme(), // Ou forcez true si c'est toujours sombre
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Pour l'instant, uniquement le thème sombre

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Adaptez la couleur de la barre de statut
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assurez-vous d'avoir un fichier Type.kt ou définissez la typo ici
        shapes = Shapes,         // Assurez-vous d'avoir un fichier Shape.kt ou définissez les formes ici
        content = content
    )
}