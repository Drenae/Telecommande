package com.telecommande.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.navigation.Screen
import com.telecommande.ui.home.composables.ContentSection
import com.telecommande.ui.home.composables.FooterSection
import com.telecommande.ui.home.composables.HeaderSection
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.ScreenPaddings

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.clearSnackbarMessage()
        }
    }

    LaunchedEffect(uiState.pairingRequiredEvent) {
        if (uiState.pairingRequiredEvent) {
            snackbarHostState.showSnackbar(
                message = "Appairage requis pour ${uiState.activeTvName ?: "la TV"}.",
                duration = SnackbarDuration.Short
            )
            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
            viewModel.consumePairingRequiredEvent()
        }
    }

    Scaffold(
        containerColor = AppColors.darkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF071018), Color(0xFF090F15), Color(0xFF05080C)),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    )
                )
        ) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x1A123646), Color(0x080A2230), Color.Transparent),
                        center = Offset(180f, 430f),
                        radius = 1050f
                    )
                )
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x121AD7FF), Color(0x060E2330), Color.Transparent),
                        center = Offset(900f, 1500f),
                        radius = 1250f
                    )
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ScreenPaddings.Horizontal, vertical = ScreenPaddings.Horizontal / 2),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderSection(
                    title = uiState.activeTvName ?: "Télécommande",
                    onPowerClick = viewModel::sendPowerCommand,
                    isConnected = uiState.isConnected,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    onStatusIndicatorClick = { navController.navigate(Screen.Settings.route) }
                )

                ContentSection(
                    modifier = Modifier.fillMaxWidth(),
                    onOkClick = viewModel::sendDpadCenterCommand,
                    onUpClick = viewModel::sendDpadUpCommand,
                    onDownClick = viewModel::sendDpadDownCommand,
                    onLeftClick = viewModel::sendDpadLeftCommand,
                    onRightClick = viewModel::sendDpadRightCommand,
                    onBackClick = viewModel::sendBackCommand,
                    onHomeClick = viewModel::sendHomeCommand,
                    volumeLevel = uiState.volumeLevel,
                    volumeMax = uiState.volumeMax,
                    isMuted = uiState.isMuted,
                    onVolumeUpClick = viewModel::sendVolumeUpCommand,
                    onVolumeDownClick = viewModel::sendVolumeDownCommand,
                    onMuteClick = viewModel::sendMuteCommand,
                    onRewindClick = viewModel::sendMediaRewindCommand,
                    onPlayPauseClick = viewModel::sendMediaPlayPauseCommand,
                    onStopClick = viewModel::sendMediaStopCommand,
                    onFastForwardClick = viewModel::sendMediaFastForwardCommand
                )

                FooterSection(
                    modifier = Modifier.fillMaxWidth(),
                    onLaunchNetflix = { viewModel.launchAppByLink("netflix://") },
                    onLaunchYouTube = { viewModel.launchAppByLink("vnd.youtube://") },
                    onLaunchPlex = { viewModel.launchAppByLink("plex://") },
                    onLaunchCrunchyroll = { viewModel.launchAppByLink("crunchyroll://") }
                )
            }
        }
    }
}
