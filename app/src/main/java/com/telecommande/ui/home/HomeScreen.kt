package com.telecommande.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.navigation.Screen
import com.telecommande.ui.home.composables.ContentSection
import com.telecommande.ui.home.composables.FooterSection
import com.telecommande.ui.home.composables.HeaderSection
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.ComponentDimensions
import com.telecommande.ui.theme.ScreenPaddings
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbarMessage()
        }
    }

    LaunchedEffect(uiState.pairingRequiredEvent) {
        if (uiState.pairingRequiredEvent) {
            snackbarHostState.showSnackbar(
                message = "Appairage requis pour ${uiState.activeTvName ?: "la TV"}. Redirection vers les paramètres...",
                duration = SnackbarDuration.Short
            )
            navController.navigate(Screen.Settings.route) {
                launchSingleTop = true
            }
            viewModel.consumePairingRequiredEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.darkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ScreenPaddings.Horizontal)
            ) {
                HeaderSection(
                    onPowerClick = viewModel::sendPowerCommand,
                    isConnected = uiState.isConnected,
                    modifier = Modifier.fillMaxWidth(),
                    onStatusIndicatorClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )

                ContentSection(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onOkClick = viewModel::sendDpadCenterCommand,
                    onUpClick = viewModel::sendDpadUpCommand,
                    onDownClick = viewModel::sendDpadDownCommand,
                    onLeftClick = viewModel::sendDpadLeftCommand,
                    onRightClick = viewModel::sendDpadRightCommand,
                    onBackClick = viewModel::sendBackCommand,
                    onHomeClick = viewModel::sendHomeCommand,
                    onKeyboardClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Fonction clavier non implémentée")
                        }
                    },
                    volumeLevel = uiState.volumeLevel,
                    volumeMax = uiState.volumeMax,
                    isMuted = uiState.isMuted,
                    onVolumeUpClick = viewModel::sendVolumeUpCommand,
                    onVolumeDownClick = viewModel::sendVolumeDownCommand,
                    onMuteClick = viewModel::sendMuteCommand
                )

                FooterSection(
                    modifier = Modifier.fillMaxWidth(),
                    onLaunchNetflix = { viewModel.launchAppByLink("netflix://") },
                    onLaunchYouTube = { viewModel.launchAppByLink("vnd.youtube://") },
                    onLaunchPlex = { viewModel.launchAppByLink("plex://") },
                    onLaunchCrunchyroll = { viewModel.launchAppByLink("crunchyroll://") }
                )
            }

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(ComponentDimensions.MediumSpacerHeight))
                        Text(
                            text = if (uiState.activeTvName != null) {
                                "Connexion à ${uiState.activeTvName}..."
                            } else {
                                "Opération en cours..."
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}