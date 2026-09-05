package com.telecommande.ui.settings.layout

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.settings.SettingsUiState
import com.telecommande.ui.settings.controls.DiscoveredTvControl
import com.telecommande.ui.settings.controls.DiscoveryEmptyControl
import com.telecommande.ui.settings.controls.DiscoveryLoadingControl
import com.telecommande.ui.settings.controls.EmptyPairedControl
import com.telecommande.ui.settings.controls.PairedTvControl
import com.telecommande.ui.settings.controls.SectionTitleControl
import com.telecommande.ui.theme.SettingsScreenDimensions
import com.telecommande.util.resolveDisplayName

@Composable
fun ContentSection(
    uiState: SettingsUiState,
    onDiscoveredTvSelected: (DiscoveredTv) -> Unit,
    onSearchClick: () -> Unit,
    onPairedTvSelected: (PairedTvInfo) -> Unit,
    onRenameClick: (PairedTvInfo) -> Unit,
    onForgetClick: (PairedTvInfo) -> Unit,
    onAlreadyConnected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
            item { DiscoveryLoadingControl() }
        } else if (discoveredTvs.isEmpty()) {
            item {
                DiscoveryEmptyControl(
                    isSearching = uiState.discoveryState.isDiscovering
                )
            }
        } else {
            items(discoveredTvs, key = { it.serviceName }) { tv ->
                DiscoveredTvControl(
                    tv = tv,
                    onClick = {
                        if (tv.ipAddress != null) {
                            onDiscoveredTvSelected(tv)
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
            SectionTitleControl(
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
            item { EmptyPairedControl(onSearch = onSearchClick) }
        } else {
            items(uiState.pairedTvs, key = { it.keystoreAlias }) { tvInfo ->
                val isActive = uiState.activeTv?.keystoreAlias == tvInfo.keystoreAlias
                val connected = isActive && uiState.remoteState.isConnected
                val displayName = tvInfo.resolveDisplayName(uiState.tvDisplayNames)

                PairedTvControl(
                    tvInfo = tvInfo,
                    displayName = displayName,
                    isActive = isActive,
                    isConnectedToThisTv = connected,
                    onConnectClick = {
                        if (!isActive || !connected) {
                            onPairedTvSelected(tvInfo)
                        } else {
                            onAlreadyConnected(displayName)
                        }
                    },
                    onRenameClick = { onRenameClick(tvInfo) },
                    onForgetClick = { onForgetClick(tvInfo) }
                )
            }
        }
    }
}
