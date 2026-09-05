package com.telecommande.ui.home.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.home.buttons.CircleButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.HomeHeaderDimensions

@Composable
fun HeaderStatusControl(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(HomeHeaderDimensions.statusDotSize)
                .background(
                    if (isConnected) {
                        AppColors.homeHeaderConnectedStatus
                    } else {
                        AppColors.homeHeaderDisconnectedStatus
                    },
                    CircleShape
                )
        )
        Spacer(Modifier.width(HomeHeaderDimensions.statusDotTextSpacing))
        Text(
            text = if (isConnected) "TV CONNECTÉE" else "TV DÉCONNECTÉE",
            color = if (isConnected) {
                AppColors.homeHeaderConnectedStatus
            } else {
                AppColors.homeHeaderDisconnectedStatus
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HeaderTvControl(
    isConnected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = when {
        isLoading -> "Connexion en cours"
        isConnected -> "Connectée"
        else -> "Déconnectée"
    }

    val statusTint = when {
        isLoading -> AppColors.homeStatusLoadingIcon
        isConnected -> AppColors.homeStatusConnectedIcon
        else -> AppColors.homeStatusDisconnectedIcon
    }

    CircleButton(
        icon = Icons.Rounded.Tv,
        contentDescription = description,
        size = HomeHeaderDimensions.statusButtonSize,
        onClick = onClick,
        modifier = modifier,
        iconScale = .58f,
        iconTint = statusTint
    )
}
