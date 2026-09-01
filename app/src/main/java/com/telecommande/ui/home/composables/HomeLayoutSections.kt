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
fun HeaderSection(title: String, modifier: Modifier = Modifier, onPowerClick: () -> Unit, isConnected: Boolean, isLoading: Boolean, onStatusIndicatorClick: () -> Unit) {
    Box(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        ConfigurableHomeButton(HomeButtons.Power.copy(size = 62.dp, iconPadding = 9.dp), onPowerClick, Modifier.align(Alignment.CenterStart))
        Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.align(Alignment.Center).padding(horizontal = 72.dp))
        StatusIndicator(isConnected, isLoading, onStatusIndicatorClick, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun StatusIndicator(isConnected: Boolean, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, connectedIconRes: Int = R.drawable.ic_status_on, disconnectedIconRes: Int = R.drawable.ic_status_off) {
    Box(modifier.size(42.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (isLoading) CircularProgressIndicator(Modifier.size(25.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.primary)
        else Icon(painterResource(if (isConnected) connectedIconRes else disconnectedIconRes), if (isConnected) "Connectée - Gérer les TV" else "Déconnectée - Gérer les TV", Modifier.size(42.dp), tint = Color.Unspecified)
    }
}

@Composable
fun ContentSection(modifier: Modifier = Modifier, onOkClick: () -> Unit, onUpClick: () -> Unit, onDownClick: () -> Unit, onLeftClick: () -> Unit, onRightClick: () -> Unit, onBackClick: () -> Unit, onHomeClick: () -> Unit, volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit, onRewindClick: () -> Unit, onPlayPauseClick: () -> Unit, onStopClick: () -> Unit, onFastForwardClick: () -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        val compactWidth = maxWidth < 360.dp
        val dpadSize = (maxWidth * 0.84f).coerceIn(246.dp, 330.dp)
        val okSize = (dpadSize * 0.37f).coerceIn(92.dp, 122.dp)
        val arrowSize = (dpadSize * 0.21f).coerceIn(52.dp, 68.dp)
        val navSize = if (compactWidth) 72.dp else 80.dp
        val sectionSpacing = if (compactWidth) 6.dp else 8.dp

        Column(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            ConstraintLayout(Modifier.size(dpadSize).background(DpadSectionSpecs.BackgroundBrush, DpadSectionSpecs.ContainerShape).border(DpadSectionSpecs.BorderWidth, DpadSectionSpecs.BorderColor, DpadSectionSpecs.ContainerShape)) {
                val (okBtn, upBtn, downBtn, leftBtn, rightBtn) = createRefs()
                ConfigurableHomeButton(HomeButtons.Ok.copy(size = okSize, iconPadding = okSize * .10f), onOkClick, Modifier.constrainAs(okBtn) { centerTo(parent) })
                ConfigurableHomeButton(HomeButtons.Up.copy(size = arrowSize), onUpClick, Modifier.constrainAs(upBtn) { top.linkTo(parent.top, 7.dp); start.linkTo(parent.start); end.linkTo(parent.end) })
                ConfigurableHomeButton(HomeButtons.Down.copy(size = arrowSize), onDownClick, Modifier.constrainAs(downBtn) { bottom.linkTo(parent.bottom, 7.dp); start.linkTo(parent.start); end.linkTo(parent.end) })
                ConfigurableHomeButton(HomeButtons.Left.copy(size = arrowSize), onLeftClick, Modifier.constrainAs(leftBtn) { top.linkTo(parent.top); bottom.linkTo(parent.bottom); start.linkTo(parent.start, 7.dp) })
                ConfigurableHomeButton(HomeButtons.Right.copy(size = arrowSize), onRightClick, Modifier.constrainAs(rightBtn) { top.linkTo(parent.top); bottom.linkTo(parent.bottom); end.linkTo(parent.end, 7.dp) })
            }

            Spacer(Modifier.height(if (compactWidth) 4.dp else 5.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (compactWidth) 44.dp else 58.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                ConfigurableHomeButton(HomeButtons.Back.copy(size = navSize, iconPadding = if (compactWidth) 10.dp else 12.dp), onBackClick)
                ConfigurableHomeButton(HomeButtons.Home.copy(size = navSize, iconPadding = if (compactWidth) 10.dp else 12.dp), onHomeClick)
            }

            Spacer(Modifier.height(sectionSpacing))
            VolumeControl(volumeLevel, volumeMax, isMuted, onVolumeUpClick, onVolumeDownClick, onMuteClick, compactWidth)
            Spacer(Modifier.height(if (compactWidth) 4.dp else 6.dp))
            MediaControls(onRewindClick, onPlayPauseClick, onStopClick, onFastForwardClick, compactWidth)
        }
    }
}

@Composable
private fun VolumeControl(volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit, compact: Boolean) {
    val currentVolumeMax = volumeMax.takeIf { it > 0 } ?: 100
    var sliderPosition by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Volume  $volumeLevel", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 0.dp), textAlign = TextAlign.Center)

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    val target = sliderPosition.roundToInt().coerceIn(0, currentVolumeMax)
                    val diff = target - volumeLevel
                    if (diff > 0) repeat(diff) { onVolumeUpClick() } else if (diff < 0) repeat(-diff) { onVolumeDownClick() }
                },
                valueRange = 0f..currentVolumeMax.toFloat(),
                steps = if (currentVolumeMax > 0) currentVolumeMax - 1 else 0,
                modifier = Modifier.weight(1f).padding(start = if (compact) 0.dp else 4.dp, end = 6.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AppSliderColors.thumbColor,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = DefaultButtonColors.DefaultBackgroundStart,
                    activeTickColor = AppSliderColors.activeTickColor,
                    inactiveTickColor = AppSliderColors.inactiveTickColor
                )
            )

            IconButton(onMuteClick, Modifier.size(if (compact) 58.dp else 62.dp)) {
                Icon(
                    painter = painterResource(if (isMuted) R.drawable.ic_remote_mute else R.drawable.ic_remote_volume),
                    contentDescription = if (isMuted) "Réactiver le son" else "Couper le son",
                    modifier = Modifier.size(if (compact) 38.dp else 42.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun MediaControls(onRewindClick: () -> Unit, onPlayPauseClick: () -> Unit, onStopClick: () -> Unit, onFastForwardClick: () -> Unit, compact: Boolean) {
    val size = if (compact) 60.dp else 68.dp
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        MediaArtworkButton(R.drawable.ic_media_rewind, "Recul", size, onRewindClick)
        MediaArtworkButton(R.drawable.ic_media_play_pause, "Lecture / Pause", size + 4.dp, onPlayPauseClick)
        MediaArtworkButton(R.drawable.ic_media_stop, "Stop", size, onStopClick)
        MediaArtworkButton(R.drawable.ic_media_fast_forward, "Avance", size, onFastForwardClick)
    }
}

@Composable
private fun MediaArtworkButton(iconRes: Int, description: String, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        Modifier
            .size(size)
            .background(DefaultButtonColors.DefaultBackgroundBrush, shape)
            .border(1.dp, DefaultButtonColors.DefaultBorder, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(iconRes), description, Modifier.size(size * .68f), tint = Color.Unspecified)
    }
}

@Composable
fun FooterSection(modifier: Modifier = Modifier, onLaunchNetflix: () -> Unit, onLaunchYouTube: () -> Unit, onLaunchPlex: () -> Unit, onLaunchCrunchyroll: () -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth().padding(top = 6.dp, bottom = 10.dp)) {
        val compact = maxWidth < 360.dp
        val buttonHeight = if (compact) 58.dp else 66.dp
        val spacing = if (compact) 7.dp else 9.dp

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ConfigurableHomeButton(HomeButtons.Netflix.copy(appLauncherIconSize = if (compact) 72.dp else 84.dp), onLaunchNetflix, Modifier.weight(1f).height(buttonHeight))
                ConfigurableHomeButton(HomeButtons.YouTube.copy(appLauncherIconSize = if (compact) 88.dp else 102.dp), onLaunchYouTube, Modifier.weight(1f).height(buttonHeight))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                ConfigurableHomeButton(HomeButtons.Plex.copy(appLauncherIconSize = if (compact) 56.dp else 64.dp), onLaunchPlex, Modifier.weight(1f).height(buttonHeight))
                ConfigurableHomeButton(HomeButtons.Crunchyroll.copy(appLauncherIconSize = if (compact) 104.dp else 120.dp), onLaunchCrunchyroll, Modifier.weight(1f).height(buttonHeight))
            }
        }
    }
}
