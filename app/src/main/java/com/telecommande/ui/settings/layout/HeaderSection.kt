package com.telecommande.ui.settings.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.settings.buttons.SettingsActionButton
import com.telecommande.ui.settings.buttons.SettingsIconButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.SettingsScreenDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
    isSearching: Boolean,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppColors.settingsTopBarBackground,
            titleContentColor = AppColors.settingsTopBarTitle,
            navigationIconContentColor = AppColors.settingsTopBarNavigationIcon,
            actionIconContentColor = AppColors.settingsTopBarActionIcon
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
                    color = AppColors.settingsTopBarEyebrow
                )
                Text(
                    text = "Paramètres",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.settingsTopBarHeading
                )
            }
        },
        navigationIcon = {
            SettingsIconButton(
                icon = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Retour",
                onClick = onBackClick
            )
        },
        actions = {
            SettingsActionButton(
                isSearching = isSearching,
                onClick = onSearchClick
            )
        }
    )
}
