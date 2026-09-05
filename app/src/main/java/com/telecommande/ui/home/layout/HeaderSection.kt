package com.telecommande.ui.home.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.home.buttons.CircleButton
import com.telecommande.ui.home.controls.HeaderStatusControl
import com.telecommande.ui.home.controls.HeaderTvControl
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.HomeHeaderDimensions

@Composable
fun HeaderSection(
    title: String,
    modifier: Modifier = Modifier,
    onPowerClick: () -> Unit,
    isConnected: Boolean,
    isLoading: Boolean,
    onStatusIndicatorClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(
                top = HomeHeaderDimensions.verticalPadding,
                bottom = HomeHeaderDimensions.verticalPadding
            )
    ) {
        CircleButton(
            icon = Icons.Rounded.PowerSettingsNew,
            contentDescription = "Power",
            size = HomeHeaderDimensions.powerButtonSize,
            onClick = onPowerClick,
            modifier = Modifier.align(Alignment.CenterStart),
            iconTint = if (isConnected) {
                AppColors.homePowerConnectedIcon
            } else {
                AppColors.homePowerDisconnectedIcon
            }
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = AppColors.homeHeaderTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HeaderStatusControl(isConnected = isConnected)
        }

        HeaderTvControl(
            isConnected = isConnected,
            isLoading = isLoading,
            onClick = onStatusIndicatorClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
