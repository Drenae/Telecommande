package com.telecommande.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.ui.manager.PairingStep
import com.telecommande.ui.settings.composables.DiscoveredTvItem
import com.telecommande.ui.settings.composables.PairedTvItem
import com.telecommande.ui.settings.composables.PinEntryDialog
import com.telecommande.ui.theme.AppColors
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
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
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
            onConfirm = viewModel::onSubmitPin,
            onDismiss = viewModel::onCancelPinEntryOrPairing,
            isLoading = uiState.isPinDialogLoading
        )
    }

    Scaffold(
        containerColor = AppColors.darkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.darkBackground,
                    titleContentColor = AppColors.appWhite,
                    navigationIconContentColor = AppColors.appWhite,
                    actionIconContentColor = AppColors.accent
                ),
                title = {
                    Text(
                        text = "Choisir une TV",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleDiscovery) {
                        Icon(
                            imageVector = if (uiState.discoveryState.isDiscovering) Icons.Default.WifiOff else Icons.Default.Add,
                            contentDescription = if (uiState.discoveryState.isDiscovering) {
                                "Arrêter la recherche"
                            } else {
                                "Ajouter une TV"
                            }
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
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.accent,
                    trackColor = AppColors.surfaceElevated
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    ConnectionSummary(
                        isConnected = uiState.remoteState.isConnected,
                        activeTvName = uiState.activeTv?.name ?: uiState.activeTv?.ipAddress,
                        statusMessage = uiState.primaryStatusMessage
                    )
                    Spacer(Modifier.height(18.dp))
                }

                item {
                    SectionTitle(
                        title = "Mes TV",
                        subtitle = if (uiState.pairedTvs.isEmpty()) {
                            "Aucune TV appairée"
                        } else {
                            "Touchez une TV pour la rendre active"
                        }
                    )
                }

                if (uiState.pairedTvs.isEmpty()) {
                    item {
                        EmptyPairedState(onSearch = viewModel::toggleDiscovery)
                    }
                } else {
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
                                            "${tvInfo.name ?: tvInfo.ipAddress} est déjà connectée."
                                        )
                                    }
                                }
                            },
                            onForgetClick = { viewModel.forgetTv(tvInfo) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(18.dp))
                    SectionTitle(
                        title = "Ajouter une TV",
                        subtitle = "TV Android détectées sur le réseau local"
                    )
                }

                if (uiState.discoveryState.isDiscovering && uiState.discoveryState.discoveredTvs.isEmpty()) {
                    item { DiscoveryLoadingState() }
                } else {
                    val newTvs = uiState.discoveryState.discoveredTvs.filter { discovered ->
                        uiState.pairedTvs.none { paired -> paired.ipAddress == discovered.ipAddress }
                    }

                    if (newTvs.isEmpty()) {
                        item {
                            DiscoveryEmptyState(
                                isSearching = uiState.discoveryState.isDiscovering,
                                onSearch = viewModel::toggleDiscovery
                            )
                        }
                    } else {
                        items(
                            items = newTvs,
                            key = { it.ipAddress ?: it.serviceName }
                        ) { tv ->
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
                    item {
                        Spacer(Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = viewModel::disconnectActiveTvFromSettings,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Se déconnecter")
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ConnectionSummary(
    isConnected: Boolean,
    activeTvName: String?,
    statusMessage: String
) {
    val statusColor = if (isConnected) AppColors.statusGreen else AppColors.textSecondary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AppColors.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = statusColor
            ) {}
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    text = activeTvName ?: "Aucune TV active",
                    color = AppColors.appWhite,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = statusMessage.ifBlank { if (isConnected) "Connectée" else "Non connectée" },
                    color = statusColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.appWhite
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun EmptyPairedState(onSearch: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.SignalWifiOff,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Aucune TV enregistrée",
                color = AppColors.appWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Ajoutez une TV présente sur le même réseau Wi-Fi.",
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onSearch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.appBlack
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Ajouter une TV")
            }
        }
    }
}

@Composable
private fun DiscoveryLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.accent)
            Spacer(Modifier.height(10.dp))
            Text("Recherche en cours...", color = AppColors.textSecondary)
        }
    }
}

@Composable
private fun DiscoveryEmptyState(
    isSearching: Boolean,
    onSearch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = null,
                tint = AppColors.textSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isSearching) "Aucune nouvelle TV pour le moment" else "Recherche arrêtée",
                color = AppColors.appWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Vérifiez que la TV est allumée et connectée au même réseau.",
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            if (!isSearching) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSearch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.accent,
                        contentColor = AppColors.appBlack
                    )
                ) {
                    Text("Rechercher")
                }
            }
        }
    }
}