package com.telecommande.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.manager.PairingStep
import com.telecommande.ui.settings.controls.ErrorDialogControl
import com.telecommande.ui.settings.controls.PinEntryControl
import com.telecommande.ui.settings.controls.RenameTvControl
import com.telecommande.ui.settings.layout.ContentSection
import com.telecommande.ui.settings.layout.HeaderSection
import com.telecommande.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var tvToRename by remember { mutableStateOf<PairedTvInfo?>(null) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearViewModelSnackbar()
        }
    }

    uiState.errorDialogContent?.let { errorMessage ->
        ErrorDialogControl(
            errorMessage = errorMessage,
            onDismiss = {
                viewModel.clearDiscoveryErrorMessageFromVm()
                viewModel.acknowledgePairingError()
            }
        )
    }

    uiState.showPinEntryDialogForTv?.let { tv ->
        PinEntryControl(
            tvToPair = tv,
            currentPin = uiState.currentPinInput,
            onPinChange = viewModel::onPinChanged,
            onConfirm = viewModel::onSubmitPin,
            onDismiss = viewModel::onCancelPinEntryOrPairing,
            isLoading = uiState.isPinDialogLoading
        )
    }

    tvToRename?.let { tvInfo ->
        RenameTvControl(
            tvInfo = tvInfo,
            renameValue = renameValue,
            hasCustomDisplayName = uiState.tvDisplayNames.containsKey(tvInfo.keystoreAlias),
            onRenameValueChange = { renameValue = it },
            onConfirm = {
                viewModel.renameTv(tvInfo, renameValue)
                tvToRename = null
            },
            onRestoreOriginalName = {
                viewModel.renameTv(tvInfo, null)
                tvToRename = null
            },
            onDismiss = { tvToRename = null }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.settingsBackgroundTop,
                        0.55f to AppColors.settingsBackgroundMiddle,
                        1.0f to AppColors.settingsBackgroundBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 1800f)
                )
            )
    ) {
        Scaffold(
            containerColor = AppColors.settingsScaffoldBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                HeaderSection(
                    isSearching = uiState.discoveryState.isDiscovering,
                    onBackClick = navController::popBackStack,
                    onSearchClick = viewModel::toggleDiscovery
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoadingOverall && uiState.pairingStep !is PairingStep.PinRequested) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.settingsLoadingIndicator,
                        trackColor = AppColors.settingsLoadingTrack
                    )
                }

                ContentSection(
                    uiState = uiState,
                    onDiscoveredTvSelected = viewModel::onDeviceSelected,
                    onSearchClick = viewModel::toggleDiscovery,
                    onPairedTvSelected = viewModel::setTvAsActive,
                    onRenameClick = { tvInfo ->
                        tvToRename = tvInfo
                        renameValue = uiState.tvDisplayNames[tvInfo.keystoreAlias] ?: ""
                    },
                    onForgetClick = viewModel::forgetTv,
                    onAlreadyConnected = { displayName ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "$displayName est déjà connectée."
                            )
                        }
                    }
                )
            }
        }
    }
}
