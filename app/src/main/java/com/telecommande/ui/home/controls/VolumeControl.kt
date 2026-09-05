@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.telecommande.ui.home.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.home.buttons.CircleButton
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.VolumeControlDimensions
import com.telecommande.util.outerRoundedShadow
import com.telecommande.util.outerShadow
import kotlin.math.roundToInt

@Composable
fun VolumeControl(
    volumeLevel: Int,
    volumeMax: Int,
    isMuted: Boolean,
    onVolumeUpClick: () -> Unit,
    onVolumeDownClick: () -> Unit,
    onMuteClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier.fillMaxWidth(),
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
