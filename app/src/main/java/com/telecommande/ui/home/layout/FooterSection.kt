package com.telecommande.ui.home.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telecommande.ui.home.controls.AppLaunchControl
import com.telecommande.ui.theme.HomeFooterDimensions

@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    onLaunchNetflix: () -> Unit,
    onLaunchYouTube: () -> Unit,
    onLaunchPlex: () -> Unit,
    onLaunchCrunchyroll: () -> Unit
) {
    AppLaunchControl(
        onLaunchNetflix = onLaunchNetflix,
        onLaunchYouTube = onLaunchYouTube,
        onLaunchPlex = onLaunchPlex,
        onLaunchCrunchyroll = onLaunchCrunchyroll,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = HomeFooterDimensions.verticalPadding,
                bottom = HomeFooterDimensions.verticalPadding
            )
    )
}
