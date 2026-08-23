package com.telecommande.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.R
import com.telecommande.ui.home.config.ConfigurableHomeButton
import com.telecommande.ui.home.config.HomeButtons
import com.telecommande.ui.theme.AppSliderColors
import com.telecommande.ui.theme.ComponentDimensions
import com.telecommande.ui.theme.DpadSectionSpecs
import com.telecommande.ui.theme.HomeBaseButtonSpecs
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    onPowerClick: () -> Unit,
    isConnected: Boolean,
    onStatusIndicatorClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ConfigurableHomeButton(
            config = HomeButtons.Power,
            onClick = onPowerClick
        )
        StatusIndicator(
            isConnected = isConnected,
            onClick = onStatusIndicatorClick
        )
    }
}

@Composable
fun StatusIndicator(
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    connectedIconRes: Int = R.drawable.ic_status_on,
    disconnectedIconRes: Int = R.drawable.ic_status_off
) {
    Icon(
        painter = painterResource(id = if (isConnected) connectedIconRes else disconnectedIconRes),
        contentDescription = if (isConnected) "Connected - Gérer les TV" else "Disconnected - Gérer les TV",
        modifier = modifier
            .size(ComponentDimensions.StatusIndicatorSize)
            .clickable(onClick = onClick),
        tint = Color.Unspecified
    )
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
    onMuteClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ConstraintLayout(
            modifier = Modifier
                .size(DpadSectionSpecs.ContainerSize)
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
                config = HomeButtons.Ok,
                onClick = onOkClick,
                modifier = Modifier.constrainAs(okBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
            ConfigurableHomeButton(
                config = HomeButtons.Up,
                onClick = onUpClick,
                modifier = Modifier.constrainAs(upBtn) {
                    top.linkTo(parent.top, margin = DpadSectionSpecs.ArrowButtonMargin)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
            ConfigurableHomeButton(
                config = HomeButtons.Down,
                onClick = onDownClick,
                modifier = Modifier.constrainAs(downBtn) {
                    bottom.linkTo(parent.bottom, margin = DpadSectionSpecs.ArrowButtonMargin)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
            ConfigurableHomeButton(
                config = HomeButtons.Left,
                onClick = onLeftClick,
                modifier = Modifier.constrainAs(leftBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = DpadSectionSpecs.ArrowButtonMargin)
                }
            )
            ConfigurableHomeButton(
                config = HomeButtons.Right,
                onClick = onRightClick,
                modifier = Modifier.constrainAs(rightBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = DpadSectionSpecs.ArrowButtonMargin)
                }
            )
        }

        Spacer(Modifier.height(ComponentDimensions.DefaultSpacerHeight))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConfigurableHomeButton(
                config = HomeButtons.Back,
                onClick = onBackClick,
                modifier = Modifier.offset(y = ComponentDimensions.NavButtonOffsetNegativeY)
            )
            ConfigurableHomeButton(
                config = HomeButtons.Home,
                onClick = onHomeClick
            )
            ConfigurableHomeButton(
                config = HomeButtons.Keyboard,
                onClick = onKeyboardClick,
                modifier = Modifier.offset(y = ComponentDimensions.NavButtonOffsetNegativeY)
            )
        }

        Spacer(Modifier.height(ComponentDimensions.LargeSpacerHeight))

        Text(
            text = "Volume: $volumeLevel / ${volumeMax.takeIf { it > 0 } ?: 100}",
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentVolumeMax = volumeMax.takeIf { it > 0 } ?: 100
            var sliderPosition by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }

            Slider(
                value = sliderPosition,
                onValueChange = { newPosition -> sliderPosition = newPosition },
                onValueChangeFinished = {
                    val targetLevel = sliderPosition.roundToInt().coerceIn(0, currentVolumeMax)
                    val currentTvLevel = volumeLevel
                    val diff = targetLevel - currentTvLevel
                    Timber.d("Slider finished: target=$targetLevel, currentTV=$currentTvLevel, diff=$diff")
                    if (diff > 0) repeat(diff) { onVolumeUpClick() }
                    else if (diff < 0) repeat(-diff) { onVolumeDownClick() }
                },
                valueRange = 0f..currentVolumeMax.toFloat(),
                steps = if (currentVolumeMax > 0) currentVolumeMax - 1 else 0,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = AppSliderColors.thumbColor,
                    activeTrackColor = AppSliderColors.activeTrackColor,
                    inactiveTrackColor = AppSliderColors.inactiveTrackColor,
                    activeTickColor = AppSliderColors.activeTickColor,
                    inactiveTickColor = AppSliderColors.inactiveTickColor
                )
            )
            IconButton(
                onClick = onMuteClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (isMuted) R.drawable.ic_mute else R.drawable.ic_volume),
                    contentDescription = "Mute",
                    modifier = Modifier.size(HomeBaseButtonSpecs.DefaultSize),
                    tint = Color.Unspecified
                )
            }
        }
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = ComponentDimensions.FooterPaddingVertical),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfigurableHomeButton(
                config = HomeButtons.Netflix,
                onClick = onLaunchNetflix,
                modifier = Modifier
                    .weight(1f)
                    .height(ComponentDimensions.AppLauncherButtonHeight)
            )
            Spacer(Modifier.weight(0.1f))
            ConfigurableHomeButton(
                config = HomeButtons.Plex,
                onClick = onLaunchPlex,
                modifier = Modifier
                    .weight(1f)
                    .height(ComponentDimensions.AppLauncherButtonHeight)
            )
        }

        Spacer(Modifier.height(ComponentDimensions.AppLauncherGridSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConfigurableHomeButton(
                config = HomeButtons.YouTube,
                onClick = onLaunchYouTube,
                modifier = Modifier
                    .weight(1f)
                    .height(ComponentDimensions.AppLauncherButtonHeight)
            )
            Spacer(Modifier.weight(0.1f))
            ConfigurableHomeButton(
                config = HomeButtons.Crunchyroll,
                onClick = onLaunchCrunchyroll,
                modifier = Modifier
                    .weight(1f)
                    .height(ComponentDimensions.AppLauncherButtonHeight)
            )
        }
    }
}