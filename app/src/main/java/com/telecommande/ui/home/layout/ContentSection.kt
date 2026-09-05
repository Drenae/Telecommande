@file:Suppress("UnusedBoxWithConstraintsScope")

package com.telecommande.ui.home.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.telecommande.ui.home.controls.DpadControl
import com.telecommande.ui.home.controls.MediaControl
import com.telecommande.ui.home.controls.NavigationControl
import com.telecommande.ui.home.controls.VolumeControl
import com.telecommande.ui.theme.HomeDpadDimensions

@Composable
fun ContentSection(
    modifier: Modifier = Modifier,
    onOkClick: () -> Unit,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    volumeLevel: Int,
    volumeMax: Int,
    isMuted: Boolean,
    onVolumeUpClick: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onMuteClick: () -> Unit,
    onRewindClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onFastForwardClick: () -> Unit
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val dpadSize = (maxWidth * .78f).coerceIn(
            HomeDpadDimensions.minimumSize,
            HomeDpadDimensions.maximumSize
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DpadControl(
                size = dpadSize,
                onOkClick = onOkClick,
                onUpClick = onUpClick,
                onDownClick = onDownClick,
                onLeftClick = onLeftClick,
                onRightClick = onRightClick
            )

            Spacer(Modifier.height(HomeDpadDimensions.bottomSpacing))

            NavigationControl(
                onBackClick = onBackClick,
                onHomeClick = onHomeClick
            )

            Spacer(Modifier.height(HomeDpadDimensions.navigationToVolumeSpacing))

            VolumeControl(
                volumeLevel = volumeLevel,
                volumeMax = volumeMax,
                isMuted = isMuted,
                onVolumeUpClick = onVolumeUpClick,
                onVolumeDownClick = onVolumeDownClick,
                onMuteClick = onMuteClick
            )

            Spacer(Modifier.height(HomeDpadDimensions.volumeToMediaSpacing))

            MediaControl(
                onRewindClick = onRewindClick,
                onPlayPauseClick = onPlayPauseClick,
                onStopClick = onStopClick,
                onFastForwardClick = onFastForwardClick
            )
        }
    }
}
