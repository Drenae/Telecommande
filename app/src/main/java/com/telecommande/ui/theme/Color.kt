package com.telecommande.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
    // Palette de base
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

    // Télécommande
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
    val remoteButtonTop = Color(0xFF1B1D27)
    val remoteButtonBottom = Color(0xFF05070B)
    val remoteButtonBorderTop = Color(0xFF555555)
    val remoteButtonBorderBottom = Color(0xFF111111)
    val dpadCenter = Color(0xFF24323B)
    val dpadMid = Color(0xFF18242B)
    val dpadEdge = Color(0xFF10171C)
    val volumePanelStart = Color(0xFF0D141B)
    val volumePanelEnd = Color(0xFF111C24)
    val volumeTrackInactive = Color(0xFF24323D)
    val volumeThumbTop = Color(0xFFF8FBFC)
    val volumeThumbBottom = Color(0xFFBFCBD1)
    val volumeThumbBorder = Color(0xFF45CFEA)
    val netflix = Color(0xFFE50914)
    val youtube = Color(0xFFFF0033)
    val plex = Color(0xFFE5A900)
    val crunchyroll = Color(0xFFFF7A00)

    // Home - Header
    val homeHeaderTitle = appWhite
    val homeHeaderConnectedStatus = remoteConnected
    val homeHeaderDisconnectedStatus = statusGray
    val homePowerConnectedIcon = statusRed
    val homePowerDisconnectedIcon = statusGreen
    val homeStatusLoadingIcon = statusAmber
    val homeStatusConnectedIcon = statusGreen
    val homeStatusDisconnectedIcon = statusGray

    // Home - D-pad
    val homeDpadShadow = Color.Black
    val homeDpadGradientTop = remoteButtonTop
    val homeDpadGradientMiddle = dpadMid
    val homeDpadGradientBottom = remoteButtonBottom
    val homeDpadBorderTop = remoteButtonBorderTop
    val homeDpadBorderBottom = remoteButtonBorderBottom
    val homeDpadIconGradientTop = appWhite
    val homeDpadIconGradientBottom = textSecondary
    val homeDpadIconMask = Color.White

    // Home - PremiumCircle
    val premiumCircleShadow = Color.Black
    val premiumCircleGradientTop = remoteButtonTop
    val premiumCircleGradientBottom = remoteButtonBottom
    val premiumCircleMainBorderTop = remoteButtonBorderTop
    val premiumCircleMainBorderBottom = remoteButtonBorderBottom
    val premiumCircleSecondaryBorder = Color.Black

    // Home - NavPill
    val navPillShadow = Color.Black
    val navPillGradientTop = remoteButtonTop
    val navPillGradientBottom = remoteButtonBottom
    val navPillMainBorderTop = remoteButtonBorderTop
    val navPillMainBorderBottom = remoteButtonBorderBottom
    val navPillSecondaryBorder = Color.Black
    val navPillLabel = textSecondary

    // Home - Volume
    val volumeActiveTrack = remoteCyan
    val volumeInactiveTrack = volumeTrackInactive
    val volumeActiveTick = transparent
    val volumeInactiveTick = transparent
    val volumeLabel = remoteTextMuted
    val volumeValue = remoteCyan
    val volumeSliderShadow = Color.Black
    val volumeSliderGradientTop = remoteButtonTop
    val volumeSliderGradientBottom = remoteButtonBottom
    val volumeSliderBorderTop = remoteButtonBorderTop
    val volumeSliderBorderBottom = remoteButtonBorderBottom
    val volumeThumbShadow = Color.Black
    val volumeThumbGradientTop = remoteButtonTop
    val volumeThumbGradientBottom = remoteButtonBottom
    val volumeThumbBorder = remoteCyan

    // Home - Applications
    val appTileShadow = Color.Black
    val appTileGradientTop = remoteButtonTop
    val appTileGradientBottom = remoteButtonBottom
    val netflixAccent = netflix
    val youtubeAccent = youtube
    val plexAccent = plex
    val crunchyrollAccent = crunchyroll

    // Settings - écran et dialogs
    val settingsErrorDialogContainer = surface
    val settingsErrorDialogTitle = appWhite
    val settingsErrorDialogText = textSecondary
    val settingsErrorDialogConfirm = accent
    val settingsRenameDialogContainer = surface
    val settingsRenameDialogTitle = appWhite
    val settingsRenameDialogText = textSecondary
    val settingsRenameTechnicalName = textSecondary
    val settingsRenameHelpText = textSecondary
    val settingsRenameConfirmBackground = accent
    val settingsRenameConfirmContent = appBlack
    val settingsRenameDismissText = textSecondary
    val settingsBackgroundTop = darkBackground
    val settingsBackgroundMiddle = surface
    val settingsBackgroundBottom = remoteDeep
    val settingsScaffoldBackground = Color.Transparent
    val settingsTopBarBackground = Color.Transparent
    val settingsTopBarTitle = appWhite
    val settingsTopBarNavigationIcon = appWhite
    val settingsTopBarActionIcon = accent
    val settingsTopBarEyebrow = accent
    val settingsTopBarHeading = appWhite
    val settingsLoadingIndicator = accent
    val settingsLoadingTrack = surfaceElevated

    // Settings - bouton retour
    val settingsIconButtonShadow = Color.Black
    val settingsIconButtonGradientTop = remoteButtonTop
    val settingsIconButtonGradientBottom = remoteButtonBottom
    val settingsIconButtonBorder = remoteButtonBorderTop.copy(alpha = 0.55f)
    val settingsIconButtonIcon = appWhite

    // Settings - bouton rechercher/arrêter
    val settingsActionButtonShadow = Color.Black
    val settingsActionButtonGradientTop = surfaceElevated
    val settingsActionButtonGradientBottom = remoteButtonBottom
    val settingsActionButtonSearchingBorder = statusAmber.copy(alpha = 0.7f)
    val settingsActionButtonIdleBorder = accent.copy(alpha = 0.6f)
    val settingsActionButtonSearchingIcon = statusAmber
    val settingsActionButtonIdleIcon = accent
    val settingsActionButtonText = appWhite

    // Settings - titre de section
    val settingsSectionIconBackground = accentMuted.copy(alpha = 0.65f)
    val settingsSectionIcon = accent
    val settingsSectionTitle = appWhite
    val settingsSectionSubtitle = textSecondary

    // Settings - état recherche
    val discoveryLoadingShadow = Color.Black
    val discoveryLoadingBackground = surface.copy(alpha = 0.92f)
    val discoveryLoadingBorder = border
    val discoveryLoadingProgress = accent
    val discoveryLoadingTitle = appWhite
    val discoveryLoadingSubtitle = textSecondary

    // Settings - cartes d'état
    val settingsStateCardShadow = Color.Black
    val settingsStateCardGradientTop = surfaceElevated
    val settingsStateCardGradientBottom = surface
    val settingsStateCardBorder = border
    val settingsStateCardIconBackground = accentMuted.copy(alpha = 0.6f)
    val settingsStateCardIcon = textSecondary
    val settingsStateCardTitle = appWhite
    val settingsStateCardSubtitle = textSecondary

    // Settings - bouton principal
    val settingsPrimaryButtonBackground = accent
    val settingsPrimaryButtonIcon = appBlack
    val settingsPrimaryButtonText = appBlack

    // TV découverte
    val discoveredTvItemShadow = Color.Black
    val discoveredTvItemGradientTop = surfaceElevated
    val discoveredTvItemGradientMiddle = surface
    val discoveredTvItemGradientBottom = remoteDeep
    val discoveredTvItemBorder = border.copy(alpha = 0.9f)
    val discoveredTvItemIconBackground = accentMuted.copy(alpha = 0.75f)
    val discoveredTvItemIconBorder = accent.copy(alpha = 0.35f)
    val discoveredTvItemIcon = accent
    val discoveredTvItemTitle = appWhite
    val discoveredTvItemSubtitle = textSecondary
    val discoveredTvItemActionBackground = accentMuted
    val discoveredTvItemActionIcon = accent

    // TV appairée
    val pairedTvItemConnectedStatus = statusGreen
    val pairedTvItemActiveStatus = statusAmber
    val pairedTvItemInactiveStatus = textSecondary
    val pairedTvItemShadow = Color.Black
    val pairedTvItemActiveGradientTop = accentMuted.copy(alpha = 0.9f)
    val pairedTvItemActiveGradientMiddle = surface
    val pairedTvItemActiveGradientBottom = remoteDeep
    val pairedTvItemInactiveGradientTop = surfaceElevated
    val pairedTvItemInactiveGradientMiddle = surface
    val pairedTvItemInactiveGradientBottom = remoteDeep
    val pairedTvItemActiveBorder = accent.copy(alpha = 0.65f)
    val pairedTvItemInactiveBorder = border.copy(alpha = 0.9f)
    val pairedTvItemActiveIconBackground = accentMuted
    val pairedTvItemInactiveIconBackground = surfacePressed
    val pairedTvItemActiveIconBorder = accent.copy(alpha = 0.5f)
    val pairedTvItemInactiveIconBorder = border
    val pairedTvItemActiveIcon = accent
    val pairedTvItemInactiveIcon = textSecondary
    val pairedTvItemTitle = appWhite
    val pairedTvItemTechnicalName = textSecondary
    val pairedTvItemIpAddress = textSecondary
    val pairedTvItemRenameBackground = accentMuted.copy(alpha = 0.7f)
    val pairedTvItemRenameIcon = accent
    val pairedTvItemForgetBackground = statusRed.copy(alpha = 0.1f)
    val pairedTvItemForgetIcon = statusRed.copy(alpha = 0.9f)

    // PIN dialog
    val pinDialogContainer = surfaceElevated
    val pinDialogTitle = appWhite
    val pinDialogText = textSecondary
    val pinDialogTvName = accent
    val pinDialogConfirmBackground = accent
    val pinDialogConfirmContent = appBlack
    val pinDialogLoadingIndicator = appBlack

    // Utilitaires d'ombre
    val outerShadowDefault = Color.Black
    val outerRoundedShadowDefault = Color.Black
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
