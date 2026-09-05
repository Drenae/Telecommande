package com.telecommande.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
    val darkBackground = Color(0xFF0B1114)
    val surface = Color(0xFF121A1E)
    val surfaceElevated = Color(0xFF182126)
    val surfacePressed = Color(0xFF202B30)
    val border = Color(0xFF27343A)

    val appWhite = Color(0xFFF4F7F8)
    val textSecondary = Color(0xFF9AA9AF)
    val appBlack = Color(0xFF071012)
    val transparent = Color.Transparent

    val accent = Color(0xFF2DD4BF)
    val accentMuted = Color(0xFF163D39)
    val statusGreen = Color(0xFF39D98A)
    val statusAmber = Color(0xFFFFB020)
    val statusRed = Color(0xFFFF5B5B)
    val statusGray = Color(0xFF808080)

    // Télécommande - surfaces et accents
    val remoteCyan = Color(0xFF19D7FF)
    val remoteDeep = Color(0xFF070B10)
    val remoteRim = Color(0xFF263540)
    val remoteButtonRim = Color(0xFF33434F)
    val remoteRaisedTop = Color(0xFF25333E)
    val remoteRaisedMid = Color(0xFF151E26)
    val remoteRaisedBottom = Color(0xFF090D12)
    val remoteConnected = Color(0xFF40E081)
    val remoteConnectedBorder = Color(0xFF208F5B)
    val remoteTextMuted = Color(0xFF9AA7B1)

    // Boutons premium
    val remoteButtonTop = Color(0xFF1B1D27)
    val remoteButtonBottom = Color(0xFF05070B)
    val remoteButtonBorderTop = Color(0xFF555555)
    val remoteButtonBorderBottom = Color(0xFF111111)

    // D-pad
    val dpadCenter = Color(0xFF24323B)
    val dpadMid = Color(0xFF18242B)
    val dpadEdge = Color(0xFF10171C)

    // Volume
    val volumePanelStart = Color(0xFF0D141B)
    val volumePanelEnd = Color(0xFF111C24)
    val volumeTrackInactive = Color(0xFF24323D)
    val volumeThumbTop = Color(0xFFF8FBFC)
    val volumeThumbBottom = Color(0xFFBFCBD1)
    val volumeThumbBorder = Color(0xFF45CFEA)

    // Applications
    val netflix = Color(0xFFE50914)
    val youtube = Color(0xFFFF0033)
    val plex = Color(0xFFE5A900)
    val crunchyroll = Color(0xFFFF7A00)
}

object DefaultButtonColors {
    val defaultBackgroundStart = AppColors.surfaceElevated
    val defaultBackgroundEnd = AppColors.surface
    val pressedBackgroundStart = AppColors.surfacePressed
    val pressedBackgroundEnd = AppColors.surfaceElevated
    val defaultBorder = AppColors.border

    val defaultShadowLight = Color.Transparent
    val defaultShadowDark = Color(0x66000000)
    val pressedShadowLight = Color.Transparent
    val pressedShadowDark = Color(0x33000000)

    val defaultBackgroundBrush = Brush.linearGradient(
        colors = listOf(defaultBackgroundStart, defaultBackgroundEnd)
    )
    val pressedBackgroundBrush = Brush.linearGradient(
        colors = listOf(pressedBackgroundStart, pressedBackgroundEnd)
    )
}

object AppSliderColors {
    val thumbColor = AppColors.appWhite
    val activeTrackColor = AppColors.accent
    val inactiveTrackColor = AppColors.surfacePressed
    val activeTickColor = Color.Transparent
    val inactiveTickColor = Color.Transparent
}
