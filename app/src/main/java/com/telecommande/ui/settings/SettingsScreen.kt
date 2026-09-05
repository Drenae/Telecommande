package com.telecommande.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.manager.PairingStep
import com.telecommande.ui.settings.composables.DiscoveredTvItem
import com.telecommande.ui.settings.composables.PairedTvItem
import com.telecommande.ui.settings.composables.PinEntryDialog
import com.telecommande.ui.theme.AppColors
import com.telecommande.util.outerRoundedShadow
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
    var tvToRename by remember { mutableStateOf<PairedTvInfo?>(null) }
    var renameValue by remember { mutableStateOf("") }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearViewModelSnackbar()
        }
    }

    uiState.errorDialogContent?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = {
                viewModel.clearDiscoveryErrorMessageFromVm()
                viewModel.acknowledgePairingError()
            },
            containerColor = AppColors.surface,
            titleContentColor = AppColors.appWhite,
            textContentColor = AppColors.textSecondary,
            title = { Text("Erreur") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearDiscoveryErrorMessageFromVm()
                        viewModel.acknowledgePairingError()
                    }
                ) {
                    Text("OK", color = AppColors.accent)
                }
            }
        )
    }

    uiState.showPinEntryDialogForTv?.let {
        PinEntryDialog(
            it,
            uiState.currentPinInput,
            viewModel::onPinChanged,
            viewModel::onSubmitPin,
            viewModel::onCancelPinEntryOrPairing,
            uiState.isPinDialogLoading
        )
    }

    tvToRename?.let { tvInfo ->
        val technicalName = tvInfo.name ?: tvInfo.ipAddress

        AlertDialog(
            onDismissRequest = { tvToRename = null },
            containerColor = AppColors.surface,
            titleContentColor = AppColors.appWhite,
            textContentColor = AppColors.textSecondary,
            title = { Text("Renommer la TV") },
            text = {
                Column {
                    Text(
                        text = "Nom réel : $technicalName",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom affiché") },
                        placeholder = { Text("Salon, Chambre…") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Ce nom est uniquement visuel. Le nom réel de la TV reste inchangé pour la connexion.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameTv(tvInfo, renameValue)
                        tvToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.accent,
                        contentColor = AppColors.appBlack
                    )
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                Row {
                    if (uiState.tvDisplayNames.containsKey(tvInfo.keystoreAlias)) {
                        TextButton(
                            onClick = {
                                viewModel.renameTv(tvInfo, null)
                                tvToRename = null
                            }
                        ) {
                            Text("Nom d'origine", color = AppColors.textSecondary)
                        }
                    }
                    TextButton(onClick = { tvToRename = null }) {
                        Text("Annuler", color = AppColors.textSecondary)
                    }
                }
            }
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.darkBackground,
                        0.55f to AppColors.surface,
                        1.0f to AppColors.remoteDeep
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 1800f)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = AppColors.appWhite,
                        navigationIconContentColor = AppColors.appWhite,
                        actionIconContentColor = AppColors.accent
                    ),
                    title = {
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "GESTION DES TV",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.accent
                            )
                            Text(
                                text = "Paramètres",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.appWhite
                            )
                        }
                    },
                    navigationIcon = {
                        SettingsIconButton(
                            icon = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Retour",
                            onClick = navController::popBackStack
                        )
                    },
                    actions = {
                        SettingsActionButton(
                            isSearching = uiState.discoveryState.isDiscovering,
                            onClick = viewModel::toggleDiscovery
                        )
                    }
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
                        color = AppColors.accent,
                        trackColor = AppColors.surfaceElevated
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 10.dp,
                        bottom = 28.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val discoveredTvs = uiState.discoveryState.discoveredTvs

                    if (uiState.discoveryState.isDiscovering && discoveredTvs.isEmpty()) {
                        item { DiscoveryLoadingState() }
                    } else if (discoveredTvs.isEmpty()) {
                        item {
                            DiscoveryEmptyState(
                                isSearching = uiState.discoveryState.isDiscovering
                            )
                        }
                    } else {
                        items(discoveredTvs, key = { it.serviceName }) { tv ->
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

                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionTitle(
                            icon = Icons.Rounded.Devices,
                            title = "Mes TV",
                            subtitle = if (uiState.pairedTvs.isEmpty()) {
                                "Aucune TV enregistrée"
                            } else {
                                "Touchez une TV pour la rendre active"
                            }
                        )
                    }

                    if (uiState.pairedTvs.isEmpty()) {
                        item { EmptyPairedState(viewModel::toggleDiscovery) }
                    } else {
                        items(uiState.pairedTvs, key = { it.keystoreAlias }) { tvInfo ->
                            val isActive = uiState.activeTv?.keystoreAlias == tvInfo.keystoreAlias
                            val connected = isActive && uiState.remoteState.isConnected
                            val displayName = uiState.tvDisplayNames[tvInfo.keystoreAlias]
                                ?: tvInfo.name
                                ?: tvInfo.ipAddress

                            PairedTvItem(
                                tvInfo = tvInfo,
                                displayName = displayName,
                                isActive = isActive,
                                isConnectedToThisTv = connected,
                                onConnectClick = {
                                    if (!isActive || !connected) {
                                        viewModel.setTvAsActive(tvInfo)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "$displayName est déjà connectée."
                                            )
                                        }
                                    }
                                },
                                onRenameClick = {
                                    tvToRename = tvInfo
                                    renameValue = uiState.tvDisplayNames[tvInfo.keystoreAlias] ?: ""
                                },
                                onForgetClick = { viewModel.forgetTv(tvInfo) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .padding(start = 10.dp)
            .size(44.dp)
            .outerRoundedShadow(
                cornerRadius = 14.dp,
                color = Color.Black,
                alpha = 0.72f,
                blurRadius = 5.dp,
                offsetY = 2.dp
            )
            .background(
                Brush.linearGradient(
                    listOf(AppColors.remoteButtonTop, AppColors.remoteButtonBottom)
                ),
                shape
            )
            .border(1.dp, AppColors.remoteButtonBorderTop.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.appWhite,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SettingsActionButton(
    isSearching: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .padding(end = 12.dp)
            .height(42.dp)
            .outerRoundedShadow(
                cornerRadius = 18.dp,
                color = Color.Black,
                alpha = 0.72f,
                blurRadius = 5.dp,
                offsetY = 2.dp
            )
            .background(
                Brush.linearGradient(
                    listOf(AppColors.surfaceElevated, AppColors.remoteButtonBottom)
                ),
                shape
            )
            .border(
                width = 1.dp,
                color = if (isSearching) {
                    AppColors.statusAmber.copy(alpha = 0.7f)
                } else {
                    AppColors.accent.copy(alpha = 0.6f)
                },
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Rounded.StopCircle else Icons.Rounded.Search,
            contentDescription = null,
            tint = if (isSearching) AppColors.statusAmber else AppColors.accent,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = if (isSearching) "ARRÊTER" else "RECHERCHER",
            color = AppColors.appWhite,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(AppColors.accentMuted.copy(alpha = 0.65f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        Column {
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
}

@Composable
private fun EmptyPairedState(onSearch: () -> Unit) {
    SettingsStateCard(
        icon = Icons.Rounded.SignalWifiOff,
        title = "Aucune TV enregistrée",
        subtitle = "Ajoutez une TV présente sur le même réseau Wi-Fi.",
        action = {
            SettingsPrimaryButton(
                icon = Icons.Rounded.Add,
                label = "Ajouter une TV",
                onClick = onSearch
            )
        }
    )
}

@Composable
private fun DiscoveryLoadingState() {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = 18.dp,
                color = Color.Black,
                alpha = 0.5f,
                blurRadius = 5.dp,
                offsetY = 2.dp
            )
            .background(AppColors.surface.copy(alpha = 0.92f), shape)
            .border(1.dp, AppColors.border, shape)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            color = AppColors.accent,
            strokeWidth = 2.5.dp
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = "Recherche en cours",
                color = AppColors.appWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Analyse du réseau local…",
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DiscoveryEmptyState(
    isSearching: Boolean
) {
    SettingsStateCard(
        icon = null,
        title = if (isSearching) "Aucune TV détectée pour le moment" else "Recherche arrêtée",
        subtitle = "Vérifiez que la TV est allumée et connectée au même réseau."
    )
}

@Composable
private fun SettingsStateCard(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = 18.dp,
                color = Color.Black,
                alpha = 0.52f,
                blurRadius = 5.dp,
                offsetY = 2.dp
            )
            .background(
                Brush.linearGradient(
                    listOf(AppColors.surfaceElevated, AppColors.surface)
                ),
                shape
            )
            .border(1.dp, AppColors.border, shape)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(AppColors.accentMuted.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        Text(
            text = title,
            color = AppColors.appWhite,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = subtitle,
            color = AppColors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        action?.let {
            Spacer(Modifier.height(12.dp))
            it()
        }
    }
}

@Composable
private fun SettingsPrimaryButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .height(42.dp)
            .background(AppColors.accent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.appBlack,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            color = AppColors.appBlack,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
