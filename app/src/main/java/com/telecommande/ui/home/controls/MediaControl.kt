package com.telecommande.ui.home.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telecommande.ui.home.buttons.CircleButton
import com.telecommande.ui.theme.MediaControlDimensions

@Composable
fun MediaControl(
    onRewindClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onFastForwardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CircleButton(
            icon = Icons.Rounded.FastRewind,
            contentDescription = "RETOUR RAPIDE",
            size = MediaControlDimensions.buttonSize,
            onClick = onRewindClick
        )
        CircleButton(
            icon = Icons.Rounded.PlayArrow,
            contentDescription = "LECTURE / PAUSE",
            size = MediaControlDimensions.buttonSize,
            onClick = onPlayPauseClick
        )
        CircleButton(
            icon = Icons.Rounded.Stop,
            contentDescription = "STOP",
            size = MediaControlDimensions.buttonSize,
            onClick = onStopClick
        )
        CircleButton(
            icon = Icons.Rounded.FastForward,
            contentDescription = "AVANCE RAPIDE",
            size = MediaControlDimensions.buttonSize,
            onClick = onFastForwardClick
        )
    }
}
