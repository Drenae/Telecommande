package com.telecommande.ui.home.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telecommande.R
import com.telecommande.ui.home.buttons.AppButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.HomeFooterDimensions

@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    onLaunchNetflix: () -> Unit,
    onLaunchYouTube: () -> Unit,
    onLaunchPlex: () -> Unit,
    onLaunchCrunchyroll: () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(
                top = HomeFooterDimensions.verticalPadding,
                bottom = HomeFooterDimensions.verticalPadding
            ),
        verticalArrangement = Arrangement.spacedBy(HomeFooterDimensions.rowSpacing)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HomeFooterDimensions.rowSpacing)
        ) {
            AppButton(
                label = "NETFLIX",
                borderColor = AppColors.netflixBorder,
                iconRes = R.drawable.ic_app_netflix,
                onClick = onLaunchNetflix,
                modifier = Modifier.weight(1f)
            )
            AppButton(
                label = "YOUTUBE",
                borderColor = AppColors.youtubeBorder,
                iconRes = R.drawable.ic_app_youtube,
                onClick = onLaunchYouTube,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HomeFooterDimensions.rowSpacing)
        ) {
            AppButton(
                label = "PLEX",
                borderColor = AppColors.plexBorder,
                iconRes = R.drawable.ic_app_plex,
                onClick = onLaunchPlex,
                modifier = Modifier.weight(1f)
            )
            AppButton(
                label = "CRUNCHYROLL",
                borderColor = AppColors.crunchyrollBorder,
                iconRes = R.drawable.ic_app_crunchy,
                onClick = onLaunchCrunchyroll,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
