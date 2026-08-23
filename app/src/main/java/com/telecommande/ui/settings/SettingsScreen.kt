package com.telecommande.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.ui.manager.PairingStep
import com.telecommande.ui.settings.composables.DiscoveredTvItem
import com.telecommande.ui.settings.composables.PairedTvItem
import com.telecommande.ui.settings.composables.PinEntryDialog
import com.telecommande.ui.theme.TvManagementSpecs
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearViewModelSnackbar()
        }
    }

    uiState.errorDialogContent?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearDiscoveryErrorMessageFromVm()
                viewModel.acknowledgePairingError()
            },
            title = { Text("Erreur") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearDiscoveryErrorMessageFromVm()
                    viewModel.acknowledgePairingError()
                }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.showPinEntryDialogForTv?.let { tvToPairForDialog ->
        PinEntryDialog(
            tvToPair = tvToPairForDialog,
            currentPin = uiState.currentPinInput,
            onPinChange = viewModel::onPinChanged,
            onConfirm = { viewModel.onSubmitPin() },
            onDismiss = { viewModel.onCancelPinEntryOrPairing() },
            isLoading = uiState.isPinDialogLoading
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gérer les TV") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDiscovery() }) {
                        Icon(
                            imageVector = if (uiState.discoveryState.isDiscovering) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = if (uiState.discoveryState.isDiscovering) "Arrêter la recherche" else "Rechercher des TV"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoadingOverall && uiState.pairingStep !is PairingStep.PinRequested) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (uiState.pairedTvs.isNotEmpty()) {
                Text(
                    "TV Appairées",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(TvManagementSpecs.ScreenSectionTitlePadding)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = TvManagementSpecs.LazyColumnHorizontalPadding)
                ) {
                    items(uiState.pairedTvs, key = { it.keystoreAlias }) { tvInfo ->
                        val isActive = uiState.activeTv?.keystoreAlias == tvInfo.keystoreAlias
                        val isConnectedToThisTv = isActive && uiState.remoteState.isConnected

                        PairedTvItem(
                            tvInfo = tvInfo,
                            isActive = isActive,
                            isConnectedToThisTv = isConnectedToThisTv,
                            onConnectClick = {
                                if (!isActive || !isConnectedToThisTv) {
                                    viewModel.setTvAsActive(tvInfo)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "${tvInfo.name ?: tvInfo.ipAddress} est déjà active et connectée."
                                        )
                                    }
                                }
                            },
                            onForgetClick = { viewModel.forgetTv(tvInfo) }
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = TvManagementSpecs.DividerVerticalPadding)
                )
            } else {
                Text(
                    "Aucune TV appairée. Lancez une recherche pour en trouver.",
                    modifier = Modifier
                        .padding(TvManagementSpecs.ScreenSectionTitlePadding)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = TvManagementSpecs.DividerVerticalPadding)
                )
            }

            Text(
                "TV Découvertes sur le réseau",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = TvManagementSpecs.ScreenSectionTitlePadding,
                    end = TvManagementSpecs.ScreenSectionTitlePadding,
                    top = TvManagementSpecs.ScreenSectionTitlePadding,
                    bottom = TvManagementSpecs.LazyColumnHorizontalPadding
                )
            )
            if (uiState.discoveryState.isDiscovering && uiState.discoveryState.discoveredTvs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TvManagementSpecs.ScreenSectionTitlePadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(TvManagementSpecs.LoadingScreenSpacerHeight))
                        Text("Recherche en cours...")
                    }
                }
            } else if (
                !uiState.discoveryState.isDiscovering &&
                uiState.discoveryState.discoveredTvs.isEmpty() &&
                uiState.pairedTvs.isEmpty()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TvManagementSpecs.ScreenSectionTitlePadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SignalWifiOff,
                        contentDescription = "Aucune TV trouvée",
                        modifier = Modifier.padding(bottom = TvManagementSpecs.EmptyStateIconBottomPadding)
                    )
                    Text("Aucune TV détectée.")
                    Text(
                        "Assurez-vous que votre TV est allumée et sur le même réseau Wi-Fi.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(TvManagementSpecs.LoadingScreenSpacerHeight))
                    Button(onClick = { viewModel.toggleDiscovery() }) {
                        Text("Relancer la recherche")
                    }
                }
            } else if (
                uiState.discoveryState.discoveredTvs.isEmpty() &&
                !uiState.discoveryState.isDiscovering
            ) {
                Text(
                    "Aucune nouvelle TV trouvée. Essayez de relancer la recherche.",
                    modifier = Modifier
                        .padding(TvManagementSpecs.ScreenSectionTitlePadding)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = TvManagementSpecs.LazyColumnHorizontalPadding)
            ) {
                items(
                    uiState.discoveryState.discoveredTvs,
                    key = { it.ipAddress ?: it.serviceName }
                ) { tv ->
                    if (uiState.pairedTvs.none { pairedTv -> pairedTv.ipAddress == tv.ipAddress }) {
                        DiscoveredTvItem(
                            tv = tv,
                            onClick = {
                                if (tv.ipAddress != null) {
                                    viewModel.onDeviceSelected(tv)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Adresse IP non disponible pour cette TV.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.activeTv != null && uiState.remoteState.isConnected) {
                Button(
                    onClick = { viewModel.disconnectActiveTvFromSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TvManagementSpecs.DisconnectButtonPadding)
                ) {
                    Text("Se déconnecter de ${uiState.activeTv?.name ?: uiState.activeTv?.ipAddress}")
                }
            }

            if (uiState.primaryStatusMessage.isNotBlank()) {
                Text(
                    text = uiState.primaryStatusMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TvManagementSpecs.ScreenSectionTitlePadding),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}