package com.telecommande.ui.home.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telecommande.ui.home.buttons.RoundedButton
import com.telecommande.ui.theme.HomeDpadDimensions

@Composable
fun NavigationControl(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HomeDpadDimensions.navigationHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RoundedButton(
            icon = Icons.Rounded.ArrowBackIosNew,
            label = "RETOUR",
            onClick = onBackClick
        )
        RoundedButton(
            icon = Icons.Rounded.Home,
            label = "ACCUEIL",
            onClick = onHomeClick
        )
    }
}
