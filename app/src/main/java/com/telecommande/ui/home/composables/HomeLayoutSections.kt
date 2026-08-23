package com.telecommande.ui.home.composables

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.R
import com.telecommande.ui.home.config.ConfigurableHomeButton
import com.telecommande.ui.home.config.HomeButtons
import com.telecommande.ui.theme.AppSliderColors
import com.telecommande.ui.theme.DefaultButtonColors
import com.telecommande.ui.theme.DpadSectionSpecs
import kotlin.math.roundToInt

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    onPowerClick: () -> Unit,
    isConnected: Boolean,
    isLoading: Boolean,
    onStatusIndicatorClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 6.dp)
    ) {
        ConfigurableHomeButton(
            config = HomeButtons.Power.copy(
                size = 58.dp,
                iconPadding = 14.dp
            ),
            onClick = onPowerClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        Text(
            text = "Télécommande",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center)
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
            .size(40.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                painter = painterResource(id = if (isConnected) connectedIconRes else disconnectedIconRes),
                contentDescription = if (isConnected) "Connectée - Gérer les TV" else "Déconnectée - Gérer les TV",
                modifier = Modifier.size(40.dp),
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
    onKeyboardClick: () -> Unit,
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
        contentAlignment = Alignment.Center
    ) {
        val compactWidth = maxWidth < 360.dp
        val dpadSize = (maxWidth * 0.74f).coerceIn(218.dp, 292.dp)
        val okSize = (dpadSize * 0.36f).coerceIn(82.dp, 108.dp)
        val arrowSize = (dpadSize * 0.18f).coerceIn(42.dp, 54.dp)
        val navSize = if (compactWidth) 54.dp else 60.dp
        val sectionSpacing = if (compactWidth) 10.dp else 16.dp

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                        iconPadding = okSize * 0.20f
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
                        top.linkTo(parent.top, margin = 6.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Down.copy(size = arrowSize),
                    onClick = onDownClick,
                    modifier = Modifier.constrainAs(downBtn) {
                        bottom.linkTo(parent.bottom, margin = 6.dp)
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
                        start.linkTo(parent.start, margin = 6.dp)
                    }
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Right.copy(size = arrowSize),
                    onClick = onRightClick,
                    modifier = Modifier.constrainAs(rightBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = 6.dp)
                    }
                )
            }

            Spacer(Modifier.height(sectionSpacing))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (compactWidth) 8.dp else 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConfigurableHomeButton(
                    config = HomeButtons.Back.copy(size = navSize, iconPadding = 15.dp),
                    onClick = onBackClick
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Home.copy(size = navSize, iconPadding = 15.dp),
                    onClick = onHomeClick
                )
                ConfigurableHomeButton(
                    config = HomeButtons.Keyboard.copy(size = navSize, iconPadding = 15.dp),
                    onClick = onKeyboardClick
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

            Spacer(Modifier.height(if (compactWidth) 6.dp else 10.dp))

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
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (isMuted) R.drawable.ic_remote_mute else R.drawable.ic_remote_volume),
                    contentDescription = if (isMuted) "Réactiver le son" else "Couper le son",
                    modifier = Modifier.size(34.dp),
                    tint = Color.Unspecified
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
    val buttonSize = if (compact) 48.dp else 54.dp
    val iconSize = if (compact) 22.dp else 25.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 4.dp else 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaButton(R.drawable.ic_media_rewind, "Recul", buttonSize, iconSize, onRewindClick)
        MediaButton(R.drawable.ic_media_play_pause, "Lecture / Pause", buttonSize, iconSize, onPlayPauseClick)
        MediaButton(R.drawable.ic_media_stop, "Stop", buttonSize, iconSize, onStopClick)
        MediaButton(R.drawable.ic_media_fast_forward, "Avance", buttonSize, iconSize, onFastForwardClick)
    }
}

@Composable
private fun MediaButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .size(size)
            .background(DefaultButtonColors.DefaultBackgroundBrush, shape)
            .border(1.dp, DefaultButtonColors.DefaultBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = Color.Unspecified
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
        val buttonHeight = if (compact) 50.dp else 54.dp
        val iconSize = if (compact) 40.dp else 46.dp
        val spacing = if (compact) 4.dp else 6.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
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