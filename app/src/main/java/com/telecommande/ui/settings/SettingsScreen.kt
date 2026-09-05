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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.manager.PairingStep
import com.telecommande.ui.settings.composables.DiscoveredTvItem
import com.telecommande.ui.settings.composables.PairedTvItem
import com.telecommande.ui.settings.composables.PinEntryDialog
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.DiscoveryLoadingDimensions
import com.telecommande.ui.theme.SettingsActionButtonDimensions
import com.telecommande.ui.theme.SettingsDialogDimensions
import com.telecommande.ui.theme.SettingsIconButtonDimensions
import com.telecommande.ui.theme.SettingsPrimaryButtonDimensions
import com.telecommande.ui.theme.SettingsScreenDimensions
import com.telecommande.ui.theme.SettingsSectionTitleDimensions
import com.telecommande.ui.theme.SettingsStateCardDimensions
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
                    Spacer(Modifier.height(SettingsDialogDimensions.renameTechnicalNameSpacing))
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it.take(32) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nom affiché") },
                        placeholder = { Text("Salon, Chambre…") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(SettingsDialogDimensions.renameHelpTextSpacing))
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
                        Column(
                            modifier = Modifier.padding(
                                start = SettingsScreenDimensions.topBarTitleStartPadding
                            )
                        ) {
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
                        start = SettingsScreenDimensions.listStartPadding,
                        end = SettingsScreenDimensions.listEndPadding,
                        top = SettingsScreenDimensions.listTopPadding,
                        bottom = SettingsScreenDimensions.listBottomPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(SettingsScreenDimensions.listItemSpacing)
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
                        Spacer(Modifier.height(SettingsScreenDimensions.pairedSectionTopSpacing))
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
    val shape = RoundedCornerShape(SettingsIconButtonDimensions.cornerRadius)

    Box(
        modifier = Modifier
            .padding(start = SettingsIconButtonDimensions.startPadding)
            .size(SettingsIconButtonDimensions.size)
            .outerRoundedShadow(
                cornerRadius = SettingsIconButtonDimensions.cornerRadius,
                color = Color.Black,
                alpha = 0.72f,
                blurRadius = SettingsIconButtonDimensions.shadowBlurRadius,
                offsetY = SettingsIconButtonDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(AppColors.remoteButtonTop, AppColors.remoteButtonBottom)
                ),
                shape
            )
            .border(
                width = SettingsIconButtonDimensions.mainBorderWidth,
                color = AppColors.remoteButtonBorderTop.copy(alpha = 0.55f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.appWhite,
            modifier = Modifier.size(SettingsIconButtonDimensions.iconSize)
        )
    }
}

@Composable
private fun SettingsActionButton(
    isSearching: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(SettingsActionButtonDimensions.cornerRadius)

    Row(
        modifier = Modifier
            .padding(end = SettingsActionButtonDimensions.endPadding)
            .height(SettingsActionButtonDimensions.height)
            .outerRoundedShadow(
                cornerRadius = SettingsActionButtonDimensions.cornerRadius,
                color = Color.Black,
                alpha = 0.72f,
                blurRadius = SettingsActionButtonDimensions.shadowBlurRadius,
                offsetY = SettingsActionButtonDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(AppColors.surfaceElevated, AppColors.remoteButtonBottom)
                ),
                shape
            )
            .border(
                width = SettingsActionButtonDimensions.mainBorderWidth,
                color = if (isSearching) {
                    AppColors.statusAmber.copy(alpha = 0.7f)
                } else {
                    AppColors.accent.copy(alpha = 0.6f)
                },
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsActionButtonDimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SettingsActionButtonDimensions.contentSpacing)
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Rounded.StopCircle else Icons.Rounded.Search,
            contentDescription = null,
            tint = if (isSearching) AppColors.statusAmber else AppColors.accent,
            modifier = Modifier.size(SettingsActionButtonDimensions.iconSize)
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
            .padding(
                horizontal = SettingsSectionTitleDimensions.horizontalPadding,
                vertical = SettingsSectionTitleDimensions.verticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(SettingsSectionTitleDimensions.iconContainerSize)
                .background(
                    AppColors.accentMuted.copy(alpha = 0.65f),
                    RoundedCornerShape(SettingsSectionTitleDimensions.iconContainerCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.accent,
                modifier = Modifier.size(SettingsSectionTitleDimensions.iconSize)
            )
        }

        Spacer(Modifier.width(SettingsSectionTitleDimensions.contentSpacing))

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
    val shape = RoundedCornerShape(DiscoveryLoadingDimensions.cornerRadius)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = DiscoveryLoadingDimensions.cornerRadius,
                color = Color.Black,
                alpha = 0.5f,
                blurRadius = DiscoveryLoadingDimensions.shadowBlurRadius,
                offsetY = DiscoveryLoadingDimensions.shadowOffsetY
            )
            .background(AppColors.surface.copy(alpha = 0.92f), shape)
            .border(
                width = DiscoveryLoadingDimensions.mainBorderWidth,
                color = AppColors.border,
                shape = shape
            )
            .padding(DiscoveryLoadingDimensions.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(DiscoveryLoadingDimensions.progressSize),
            color = AppColors.accent,
            strokeWidth = DiscoveryLoadingDimensions.progressStrokeWidth
        )
        Spacer(Modifier.width(DiscoveryLoadingDimensions.progressTextSpacing))
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
    val shape = RoundedCornerShape(SettingsStateCardDimensions.cornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = SettingsStateCardDimensions.cornerRadius,
                color = Color.Black,
                alpha = 0.52f,
                blurRadius = SettingsStateCardDimensions.shadowBlurRadius,
                offsetY = SettingsStateCardDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(AppColors.surfaceElevated, AppColors.surface)
                ),
                shape
            )
            .border(
                width = SettingsStateCardDimensions.mainBorderWidth,
                color = AppColors.border,
                shape = shape
            )
            .padding(SettingsStateCardDimensions.contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(SettingsStateCardDimensions.iconContainerSize)
                    .background(AppColors.accentMuted.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(SettingsStateCardDimensions.iconSize)
                )
            }
            Spacer(Modifier.height(SettingsStateCardDimensions.iconBottomSpacing))
        }

        Text(
            text = title,
            color = AppColors.appWhite,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(SettingsStateCardDimensions.titleSubtitleSpacing))
        Text(
            text = subtitle,
            color = AppColors.textSecondary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        action?.let {
            Spacer(Modifier.height(SettingsStateCardDimensions.actionTopSpacing))
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
    val shape = RoundedCornerShape(SettingsPrimaryButtonDimensions.cornerRadius)

    Row(
        modifier = Modifier
            .height(SettingsPrimaryButtonDimensions.height)
            .background(AppColors.accent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsPrimaryButtonDimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SettingsPrimaryButtonDimensions.contentSpacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.appBlack,
            modifier = Modifier.size(SettingsPrimaryButtonDimensions.iconSize)
        )
        Text(
            text = label,
            color = AppColors.appBlack,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
