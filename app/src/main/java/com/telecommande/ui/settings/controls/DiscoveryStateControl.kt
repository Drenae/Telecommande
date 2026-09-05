package com.telecommande.ui.settings.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SignalWifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.telecommande.ui.settings.buttons.SettingsPrimaryButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.DiscoveryLoadingDimensions
import com.telecommande.ui.theme.SettingsStateCardDimensions
import com.telecommande.util.outerRoundedShadow

@Composable
fun DiscoveryLoadingControl(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(DiscoveryLoadingDimensions.cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = DiscoveryLoadingDimensions.cornerRadius,
                color = AppColors.discoveryLoadingShadow,
                alpha = 0.5f,
                blurRadius = DiscoveryLoadingDimensions.shadowBlurRadius,
                offsetY = DiscoveryLoadingDimensions.shadowOffsetY
            )
            .background(AppColors.discoveryLoadingBackground, shape)
            .border(
                width = DiscoveryLoadingDimensions.mainBorderWidth,
                color = AppColors.discoveryLoadingBorder,
                shape = shape
            )
            .padding(DiscoveryLoadingDimensions.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(DiscoveryLoadingDimensions.progressSize),
            color = AppColors.discoveryLoadingProgress,
            strokeWidth = DiscoveryLoadingDimensions.progressStrokeWidth
        )
        Spacer(Modifier.width(DiscoveryLoadingDimensions.progressTextSpacing))
        Column {
            Text(
                text = "Recherche en cours",
                color = AppColors.discoveryLoadingTitle,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Analyse du réseau local…",
                color = AppColors.discoveryLoadingSubtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DiscoveryEmptyControl(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    SettingsStateControl(
        icon = null,
        title = if (isSearching) "Aucune TV détectée pour le moment" else "Recherche arrêtée",
        subtitle = "Vérifiez que la TV est allumée et connectée au même réseau.",
        modifier = modifier
    )
}

@Composable
fun EmptyPairedControl(
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsStateControl(
        icon = Icons.Rounded.SignalWifiOff,
        title = "Aucune TV enregistrée",
        subtitle = "Ajoutez une TV présente sur le même réseau Wi-Fi.",
        modifier = modifier,
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
private fun SettingsStateControl(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(SettingsStateCardDimensions.cornerRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .outerRoundedShadow(
                cornerRadius = SettingsStateCardDimensions.cornerRadius,
                color = AppColors.settingsStateCardShadow,
                alpha = 0.52f,
                blurRadius = SettingsStateCardDimensions.shadowBlurRadius,
                offsetY = SettingsStateCardDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        AppColors.settingsStateCardGradientTop,
                        AppColors.settingsStateCardGradientBottom
                    )
                ),
                shape
            )
            .border(
                width = SettingsStateCardDimensions.mainBorderWidth,
                color = AppColors.settingsStateCardBorder,
                shape = shape
            )
            .padding(SettingsStateCardDimensions.contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(SettingsStateCardDimensions.iconContainerSize)
                    .background(AppColors.settingsStateCardIconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.settingsStateCardIcon,
                    modifier = Modifier.size(SettingsStateCardDimensions.iconSize)
                )
            }
            Spacer(Modifier.height(SettingsStateCardDimensions.iconBottomSpacing))
        }

        Text(
            text = title,
            color = AppColors.settingsStateCardTitle,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(SettingsStateCardDimensions.titleSubtitleSpacing))
        Text(
            text = subtitle,
            color = AppColors.settingsStateCardSubtitle,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )

        action?.let {
            Spacer(Modifier.height(SettingsStateCardDimensions.actionTopSpacing))
            it()
        }
    }
}
