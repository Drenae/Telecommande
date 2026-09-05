@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@file:Suppress("UnusedBoxWithConstraintsScope")

package com.telecommande.ui.home.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.ui.home.buttons.CircleButton
import com.telecommande.ui.home.buttons.DpadButton
import com.telecommande.ui.home.buttons.RoundedButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.HomeDpadDimensions
import com.telecommande.ui.theme.MediaControlDimensions
import com.telecommande.ui.theme.VolumeControlDimensions
import com.telecommande.util.outerRoundedShadow
import com.telecommande.util.outerShadow
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CaretDown
import compose.icons.fontawesomeicons.solid.CaretLeft
import compose.icons.fontawesomeicons.solid.CaretRight
import compose.icons.fontawesomeicons.solid.CaretUp
import kotlin.math.roundToInt

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
        val dpad = (maxWidth * .78f).coerceIn(
            HomeDpadDimensions.minimumSize,
            HomeDpadDimensions.maximumSize
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ConstraintLayout(
                Modifier
                    .size(dpad)
                    .outerShadow(
                        color = AppColors.homeDpadShadow,
                        alpha = 0.9f,
                        blurRadius = HomeDpadDimensions.shadowBlurRadius,
                        offsetY = HomeDpadDimensions.shadowOffsetY
                    )
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AppColors.homeDpadGradientTop,
                                0.42f to AppColors.homeDpadGradientMiddle,
                                1.0f to AppColors.homeDpadGradientBottom
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 700f)
                        ),
                        CircleShape
                    )
                    .border(
                        width = HomeDpadDimensions.mainBorderWidth,
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AppColors.homeDpadBorderTop,
                                0.18f to AppColors.homeDpadBorderBottom
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 700f)
                        ),
                        shape = CircleShape
                    )
            ) {
                val (ok, up, down, left, right) = createRefs()

                CircleButton(
                    icon = Icons.Rounded.Check,
                    contentDescription = "OK",
                    size = dpad * .31f,
                    onClick = onOkClick,
                    modifier = Modifier.constrainAs(ok) {
                        centerTo(parent)
                    },
                    iconScale = .62f
                )

                DpadButton(
                    icon = FontAwesomeIcons.Solid.CaretUp,
                    contentDescription = "Haut",
                    size = dpad * .40f,
                    onClick = onUpClick,
                    modifier = Modifier.constrainAs(up) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                DpadButton(
                    icon = FontAwesomeIcons.Solid.CaretDown,
                    contentDescription = "Bas",
                    size = dpad * .40f,
                    onClick = onDownClick,
                    modifier = Modifier.constrainAs(down) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                DpadButton(
                    icon = FontAwesomeIcons.Solid.CaretLeft,
                    contentDescription = "Gauche",
                    size = dpad * .40f,
                    onClick = onLeftClick,
                    modifier = Modifier.constrainAs(left) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                )

                DpadButton(
                    icon = FontAwesomeIcons.Solid.CaretRight,
                    contentDescription = "Droite",
                    size = dpad * .40f,
                    onClick = onRightClick,
                    modifier = Modifier.constrainAs(right) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                )
            }

            Spacer(Modifier.height(HomeDpadDimensions.bottomSpacing))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HomeDpadDimensions.navigationHorizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RoundedButton(Icons.Rounded.ArrowBackIosNew, "RETOUR", onBackClick)
                RoundedButton(Icons.Rounded.Home, "ACCUEIL", onHomeClick)
            }

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

            MediaControls(
                rewind = onRewindClick,
                play = onPlayPauseClick,
                stop = onStopClick,
                fastForward = onFastForwardClick
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
    onMuteClick: () -> Unit
) {
    val max = volumeMax.takeIf { it > 0 } ?: 100
    var position by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }

    val sliderColors = SliderDefaults.colors(
        activeTrackColor = AppColors.volumeActiveTrack,
        inactiveTrackColor = AppColors.volumeInactiveTrack,
        activeTickColor = AppColors.volumeActiveTick,
        inactiveTickColor = AppColors.volumeInactiveTick
    )

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(
                    start = VolumeControlDimensions.contentStartPadding,
                    end = VolumeControlDimensions.contentEndPadding
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VolumeControlDimensions.labelHorizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VOLUME",
                    color = AppColors.volumeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$volumeLevel",
                    color = AppColors.volumeValue,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(VolumeControlDimensions.labelToSliderSpacing))

            val sliderShape = RoundedCornerShape(VolumeControlDimensions.sliderCornerRadius)

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(VolumeControlDimensions.sliderHeight)
                    .outerRoundedShadow(
                        cornerRadius = VolumeControlDimensions.sliderCornerRadius,
                        color = AppColors.volumeSliderShadow,
                        alpha = 0.9f,
                        blurRadius = VolumeControlDimensions.sliderShadowBlurRadius,
                        offsetY = VolumeControlDimensions.sliderShadowOffsetY
                    )
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AppColors.volumeSliderGradientTop,
                                0.35f to AppColors.volumeSliderGradientBottom
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 500f)
                        ),
                        sliderShape
                    )
                    .border(
                        width = VolumeControlDimensions.sliderMainBorderWidth,
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AppColors.volumeSliderBorderTop,
                                0.15f to AppColors.volumeSliderBorderBottom
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 500f)
                        ),
                        shape = sliderShape
                    )
                    .padding(horizontal = VolumeControlDimensions.sliderHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = position,
                    onValueChange = { position = it },
                    onValueChangeFinished = {
                        val target = position.roundToInt().coerceIn(0, max)
                        val difference = target - volumeLevel

                        if (difference > 0) {
                            repeat(difference) { onVolumeUpClick() }
                        } else if (difference < 0) {
                            repeat(-difference) { onVolumeDownClick() }
                        }
                    },
                    valueRange = 0f..max.toFloat(),
                    steps = 0,
                    modifier = Modifier.fillMaxWidth(),
                    colors = sliderColors,
                    thumb = {
                        Box(
                            Modifier
                                .size(VolumeControlDimensions.thumbSize)
                                .outerShadow(
                                    color = AppColors.volumeThumbShadow,
                                    alpha = 0.9f,
                                    blurRadius = VolumeControlDimensions.thumbShadowBlurRadius,
                                    offsetY = VolumeControlDimensions.thumbShadowOffsetY
                                )
                                .background(
                                    Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.0f to AppColors.volumeThumbGradientTop,
                                            0.45f to AppColors.volumeThumbGradientBottom
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, 120f)
                                    ),
                                    CircleShape
                                )
                                .border(
                                    width = VolumeControlDimensions.thumbBorderWidth,
                                    color = AppColors.volumeThumbBorder,
                                    shape = CircleShape
                                )
                        )
                    },
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            colors = sliderColors,
                            thumbTrackGapSize = VolumeControlDimensions.trackThumbGapSize,
                            trackInsideCornerSize = VolumeControlDimensions.trackInsideCornerSize
                        )
                    }
                )
            }
        }

        CircleButton(
            icon = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
            contentDescription = "Muet",
            size = VolumeControlDimensions.muteButtonSize,
            onClick = onMuteClick,
            modifier = Modifier.padding(top = VolumeControlDimensions.muteButtonTopPadding)
        )
    }
}

@Composable
private fun MediaControls(
    rewind: () -> Unit,
    play: () -> Unit,
    stop: () -> Unit,
    fastForward: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MediaButton(Icons.Rounded.FastRewind, "RETOUR RAPIDE", rewind)
        MediaButton(Icons.Rounded.PlayArrow, "LECTURE / PAUSE", play)
        MediaButton(Icons.Rounded.Stop, "STOP", stop)
        MediaButton(Icons.Rounded.FastForward, "AVANCE RAPIDE", fastForward)
    }
}

@Composable
private fun MediaButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    CircleButton(
        icon = icon,
        contentDescription = label,
        size = MediaControlDimensions.buttonSize,
        onClick = onClick
    )
}
