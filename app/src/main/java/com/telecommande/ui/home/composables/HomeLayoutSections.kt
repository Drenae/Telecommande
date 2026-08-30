package com.telecommande.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.R
import com.telecommande.ui.home.config.ConfigurableHomeButton
import com.telecommande.ui.home.config.HomeButtons
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.AppSliderColors
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.DpadSectionSpecs
import kotlin.math.roundToInt

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
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        ConfigurableHomeButton(
            config = HomeButtons.Power.copy(
                size = 60.dp,
                iconPadding = 13.dp
            ),
            onClick = onPowerClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 70.dp)
        )

        StatusIndicator(
            isConnected = isConnected,
            isLoading = isLoading,
            onClick = onStatusIndicatorClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun StatusIndicator(
    isConnected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    connectedIconRes: Int = R.drawable.ic_status_on,
    disconnectedIconRes: Int = R.drawable.ic_status_off
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(25.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                painter = painterResource(id = if (isConnected) connectedIconRes else disconnectedIconRes),
                contentDescription = if (isConnected) "Connectée - Gérer les TV" else "Déconnectée - Gérer les TV",
                modifier = Modifier.size(42.dp),
                tint = Color.Unspecified
            )
        }
    }
}

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
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        val compactWidth = maxWidth < 360.dp
        val dpadSize = (maxWidth * 0.84f).coerceIn(246.dp, 330.dp)
        val okSize = (dpadSize * 0.37f).coerceIn(92.dp, 122.dp)
        val arrowSize = (dpadSize * 0.19f).coerceIn(48.dp, 62.dp)
        val navSize = if (compactWidth) 68.dp else 76.dp
        val sectionSpacing = if (compactWidth) 8.dp else 11.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .size(dpadSize)
                    .background(
                        brush = DpadSectionSpecs.BackgroundBrush,
                        shape = DpadSectionSpecs.ContainerShape
                    )
                    .border(
                        width = DpadSectionSpecs.BorderWidth,
                        color = DpadSectionSpecs.BorderColor,
                        shape = DpadSectionSpecs.ContainerShape
                    )
            ) {
                val (okBtn, upBtn, downBtn, leftBtn, rightBtn) = createRefs()

                ConfigurableHomeButton(
                    config = HomeButtons.Ok.copy(
                        size = okSize,
                        iconPadding = okSize * 0.18f
                    ),
                    onClick = onOkClick,
                    modifier = Modifier.constrainAs(okBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Up.copy(size = arrowSize),
                    onClick = onUpClick,
                    modifier = Modifier.constrainAs(upBtn) {
                        top.linkTo(parent.top, margin = 7.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Down.copy(size = arrowSize),
                    onClick = onDownClick,
                    modifier = Modifier.constrainAs(downBtn) {
                        bottom.linkTo(parent.bottom, margin = 7.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Left.copy(size = arrowSize),
                    onClick = onLeftClick,
                    modifier = Modifier.constrainAs(leftBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start, margin = 7.dp)
                    }
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Right.copy(size = arrowSize),
                    onClick = onRightClick,
                    modifier = Modifier.constrainAs(rightBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 7.dp)
                    }
                )
            }

            Spacer(Modifier.height(sectionSpacing))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = if (compactWidth) 52.dp else 68.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfigurableHomeButton(
                    config = HomeButtons.Back.copy(size = navSize, iconPadding = if (compactWidth) 16.dp else 18.dp),
                    onClick = onBackClick
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Home.copy(size = navSize, iconPadding = if (compactWidth) 16.dp else 18.dp),
                    onClick = onHomeClick
                )
            }

            Spacer(Modifier.height(sectionSpacing))

            VolumeControl(
                volumeLevel = volumeLevel,
                volumeMax = volumeMax,
                isMuted = isMuted,
                onVolumeUpClick = onVolumeUpClick,
                onVolumeDownClick = onVolumeDownClick,
                onMuteClick = onMuteClick,
                compact = compactWidth
            )

            Spacer(Modifier.height(if (compactWidth) 5.dp else 8.dp))

            MediaControls(
                onRewindClick = onRewindClick,
                onPlayPauseClick = onPlayPauseClick,
                onStopClick = onStopClick,
                onFastForwardClick = onFastForwardClick,
                compact = compactWidth
            )
        }
    }
}

@Composable
private fun VolumeControl(
    volumeLevel: Int,
    volumeMax: Int,
    isMuted: Boolean,
    onVolumeUpClick: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onMuteClick: () -> Unit,
    compact: Boolean
) {
    val currentVolumeMax = volumeMax.takeIf { it > 0 } ?: 100
    var sliderPosition by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Volume : $volumeLevel / $currentVolumeMax",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 2.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    val targetLevel = sliderPosition.roundToInt().coerceIn(0, currentVolumeMax)
                    val diff = targetLevel - volumeLevel
                    if (diff > 0) repeat(diff) { onVolumeUpClick() }
                    else if (diff < 0) repeat(-diff) { onVolumeDownClick() }
                },
                valueRange = 0f..currentVolumeMax.toFloat(),
                steps = if (currentVolumeMax > 0) currentVolumeMax - 1 else 0,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (compact) 0.dp else 4.dp, end = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AppSliderColors.thumbColor,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = DefaultButtonColors.DefaultBackgroundStart,
                    activeTickColor = AppSliderColors.activeTickColor,
                    inactiveTickColor = AppSliderColors.inactiveTickColor
                )
            )

            IconButton(
                onClick = onMuteClick,
                modifier = Modifier.size(if (compact) 54.dp else 58.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                    contentDescription = if (isMuted) "Réactiver le son" else "Couper le son",
                    modifier = Modifier.size(if (compact) 32.dp else 36.dp),
                    tint = AppColors.accent
                )
            }
        }
    }
}

@Composable
private fun MediaControls(
    onRewindClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onFastForwardClick: () -> Unit,
    compact: Boolean
) {
    val buttonSize = if (compact) 58.dp else 66.dp
    val iconSize = if (compact) 27.dp else 31.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaButton(Icons.Rounded.FastRewind, "Recul", buttonSize, iconSize, onRewindClick)
        MediaButton(Icons.Rounded.PlayArrow, "Lecture / Pause", buttonSize, iconSize, onPlayPauseClick)
        MediaButton(Icons.Rounded.Stop, "Stop", buttonSize, iconSize, onStopClick)
        MediaButton(Icons.Rounded.FastForward, "Avance", buttonSize, iconSize, onFastForwardClick)
    }
}

@Composable
private fun MediaButton(
    icon: ImageVector,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .size(size)
            .background(DefaultButtonColors.DefaultBackgroundBrush, shape)
            .border(1.dp, DefaultButtonColors.DefaultBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = AppColors.appWhite
        )
    }
}

@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    onLaunchNetflix: () -> Unit,
    onLaunchYouTube: () -> Unit,
    onLaunchPlex: () -> Unit,
    onLaunchCrunchyroll: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        val compact = maxWidth < 360.dp
        val buttonHeight = if (compact) 56.dp else 62.dp
        val iconSize = if (compact) 42.dp else 48.dp
        val horizontalSpacing = if (compact) 7.dp else 9.dp
        val verticalSpacing = if (compact) 7.dp else 9.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                ConfigurableHomeButton(
                    config = HomeButtons.Netflix.copy(appLauncherIconSize = iconSize),
                    onClick = onLaunchNetflix,
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )
                ConfigurableHomeButton(
                    config = HomeButtons.YouTube.copy(appLauncherIconSize = iconSize),
                    onClick = onLaunchYouTube,
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                ConfigurableHomeButton(
                    config = HomeButtons.Plex.copy(appLauncherIconSize = iconSize),
                    onClick = onLaunchPlex,
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Crunchyroll.copy(appLauncherIconSize = iconSize),
                    onClick = onLaunchCrunchyroll,
                    modifier = Modifier.weight(1f).height(buttonHeight)
                )
            }
        }
    }
}
