@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@file:Suppress("UnusedBoxWithConstraintsScope", "UNUSED_PARAMETER")

package com.telecommande.ui.home.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.R
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.AppTileDimensions
import com.telecommande.ui.theme.HomeDpadDimensions
import com.telecommande.ui.theme.HomeFooterDimensions
import com.telecommande.ui.theme.HomeHeaderDimensions
import com.telecommande.ui.theme.MediaControlDimensions
import com.telecommande.ui.theme.NavPillDimensions
import com.telecommande.ui.theme.PremiumCircleDimensions
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
fun HeaderSection(
    title: String,
    modifier: Modifier = Modifier,
    onPowerClick: () -> Unit,
    isConnected: Boolean,
    isLoading: Boolean,
    onStatusIndicatorClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(
                top = HomeHeaderDimensions.verticalPadding,
                bottom = HomeHeaderDimensions.verticalPadding
            )
    ) {
        PremiumCircle(
            icon = Icons.Rounded.PowerSettingsNew,
            desc = "Power",
            size = HomeHeaderDimensions.powerButtonSize,
            onClick = onPowerClick,
            modifier = Modifier.align(Alignment.CenterStart),
            iconTint = if (isConnected) AppColors.statusRed else AppColors.statusGreen
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = AppColors.appWhite,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(HomeHeaderDimensions.statusDotSize)
                        .background(
                            if (isConnected) AppColors.remoteConnected else AppColors.statusGray,
                            CircleShape
                        )
                )
                Spacer(Modifier.width(HomeHeaderDimensions.statusDotTextSpacing))
                Text(
                    text = if (isConnected) "TV CONNECTÉE" else "TV DÉCONNECTÉE",
                    color = if (isConnected) AppColors.remoteConnected else AppColors.statusGray,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
    modifier: Modifier = Modifier
) {
    val description = when {
        isLoading -> "Connexion en cours"
        isConnected -> "Connectée"
        else -> "Déconnectée"
    }

    val statusTint = when {
        isLoading -> AppColors.statusAmber
        isConnected -> AppColors.statusGreen
        else -> AppColors.statusGray
    }

    PremiumCircle(
        icon = Icons.Rounded.Tv,
        desc = description,
        size = HomeHeaderDimensions.statusButtonSize,
        onClick = onClick,
        modifier = modifier,
        iconScale = .58f,
        iconTint = statusTint
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
                        color = Color.Black,
                        alpha = 0.9f,
                        blurRadius = HomeDpadDimensions.shadowBlurRadius,
                        offsetY = HomeDpadDimensions.shadowOffsetY
                    )
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AppColors.remoteButtonTop,
                                0.42f to AppColors.dpadMid,
                                1.0f to AppColors.remoteButtonBottom
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
                                0.0f to AppColors.remoteButtonBorderTop,
                                0.18f to AppColors.remoteButtonBorderBottom
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 700f)
                        ),
                        shape = CircleShape
                    )
            ) {
                val (ok, u, d, l, r) = createRefs()

                PremiumCircle(
                    icon = Icons.Rounded.Check,
                    desc = "OK",
                    size = dpad * .31f,
                    onClick = onOkClick,
                    modifier = Modifier.constrainAs(ok) {
                        centerTo(parent)
                    },
                    iconScale = .62f
                )

                DpadIcon(
                    icon = FontAwesomeIcons.Solid.CaretUp,
                    desc = "Haut",
                    size = dpad * .40f,
                    onClick = onUpClick,
                    modifier = Modifier.constrainAs(u) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                DpadIcon(
                    icon = FontAwesomeIcons.Solid.CaretDown,
                    desc = "Bas",
                    size = dpad * .40f,
                    onClick = onDownClick,
                    modifier = Modifier.constrainAs(d) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                )

                DpadIcon(
                    icon = FontAwesomeIcons.Solid.CaretLeft,
                    desc = "Gauche",
                    size = dpad * .40f,
                    onClick = onLeftClick,
                    modifier = Modifier.constrainAs(l) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                )

                DpadIcon(
                    icon = FontAwesomeIcons.Solid.CaretRight,
                    desc = "Droite",
                    size = dpad * .40f,
                    onClick = onRightClick,
                    modifier = Modifier.constrainAs(r) {
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
                NavPill(Icons.Rounded.ArrowBackIosNew, "RETOUR", onBackClick)
                NavPill(Icons.Rounded.Home, "ACCUEIL", onHomeClick)
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
                rew = onRewindClick,
                play = onPlayPauseClick,
                stop = onStopClick,
                ff = onFastForwardClick
            )
        }
    }
}

@Composable
private fun GradientIcon(
    icon: ImageVector,
    desc: String,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    brush: Brush = Brush.verticalGradient(
        listOf(AppColors.appWhite, AppColors.textSecondary)
    )
) {
    if (tint != null) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = tint,
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = Color.White,
            modifier = modifier
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = brush,
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                }
        )
    }
}

@Composable
private fun DpadIcon(
    icon: ImageVector,
    desc: String,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        GradientIcon(
            icon = icon,
            desc = desc,
            modifier = Modifier.size(size * .72f)
        )
    }
}

@Composable
private fun PremiumCircle(
    icon: ImageVector,
    desc: String,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconScale: Float = .60f,
    iconTint: Color? = null
) {
    Box(
        modifier
            .size(size)
            .outerShadow(
                color = Color.Black,
                alpha = 0.9f,
                blurRadius = PremiumCircleDimensions.shadowBlurRadius,
                offsetY = PremiumCircleDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.remoteButtonTop,
                        0.35f to AppColors.remoteButtonBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 500f)
                ),
                CircleShape
            )
            .border(
                width = PremiumCircleDimensions.mainBorderWidth,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.remoteButtonBorderTop,
                        0.15f to AppColors.remoteButtonBorderBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 500f)
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .border(
                    width = PremiumCircleDimensions.secondaryBorderWidth,
                    color = Color.Black,
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            GradientIcon(
                icon = icon,
                desc = desc,
                modifier = Modifier.size(size * iconScale),
                tint = iconTint
            )
        }
    }
}

@Composable
private fun NavPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(NavPillDimensions.cornerRadius)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(NavPillDimensions.width)
                .height(NavPillDimensions.height)
                .outerRoundedShadow(
                    cornerRadius = NavPillDimensions.cornerRadius,
                    color = Color.Black,
                    alpha = 0.9f,
                    blurRadius = NavPillDimensions.shadowBlurRadius,
                    offsetY = NavPillDimensions.shadowOffsetY
                )
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to AppColors.remoteButtonTop,
                            0.35f to AppColors.remoteButtonBottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 500f)
                    ),
                    shape
                )
                .border(
                    width = NavPillDimensions.mainBorderWidth,
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to AppColors.remoteButtonBorderTop,
                            0.15f to AppColors.remoteButtonBorderBottom
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 500f)
                    ),
                    shape = shape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = NavPillDimensions.secondaryBorderWidth,
                        color = Color.Black,
                        shape = shape
                    )
                    .clip(shape),
                contentAlignment = Alignment.Center
            ) {
                GradientIcon(
                    icon = icon,
                    desc = label,
                    modifier = Modifier.size(NavPillDimensions.iconSize)
                )
            }
        }

        Spacer(Modifier.height(NavPillDimensions.labelTopSpacing))

        Text(
            text = label,
            color = AppColors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
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
    var pos by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }

    val sliderColors = SliderDefaults.colors(
        activeTrackColor = AppColors.remoteCyan,
        inactiveTrackColor = AppColors.volumeTrackInactive,
        activeTickColor = AppColors.transparent,
        inactiveTickColor = AppColors.transparent
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
                    color = AppColors.remoteTextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$volumeLevel",
                    color = AppColors.remoteCyan,
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
                        color = Color.Black,
                        alpha = 0.9f,
                        blurRadius = VolumeControlDimensions.sliderShadowBlurRadius,
                        offsetY = VolumeControlDimensions.sliderShadowOffsetY
                    )
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to AppColors.remoteButtonTop,
                                0.35f to AppColors.remoteButtonBottom
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
                                0.0f to AppColors.remoteButtonBorderTop,
                                0.15f to AppColors.remoteButtonBorderBottom
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
                    value = pos,
                    onValueChange = { pos = it },
                    onValueChangeFinished = {
                        val target = pos.roundToInt().coerceIn(0, max)
                        val diff = target - volumeLevel

                        if (diff > 0) {
                            repeat(diff) { onVolumeUpClick() }
                        } else if (diff < 0) {
                            repeat(-diff) { onVolumeDownClick() }
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
                                    color = Color.Black,
                                    alpha = 0.9f,
                                    blurRadius = VolumeControlDimensions.thumbShadowBlurRadius,
                                    offsetY = VolumeControlDimensions.thumbShadowOffsetY
                                )
                                .background(
                                    Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.0f to AppColors.remoteButtonTop,
                                            0.45f to AppColors.remoteButtonBottom
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, 120f)
                                    ),
                                    CircleShape
                                )
                                .border(
                                    width = VolumeControlDimensions.thumbBorderWidth,
                                    color = AppColors.remoteCyan,
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

        PremiumCircle(
            icon = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
            desc = "Muet",
            size = VolumeControlDimensions.muteButtonSize,
            onClick = onMuteClick,
            modifier = Modifier.padding(top = VolumeControlDimensions.muteButtonTopPadding)
        )
    }
}

@Composable
private fun MediaControls(
    rew: () -> Unit,
    play: () -> Unit,
    stop: () -> Unit,
    ff: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Media(Icons.Rounded.FastRewind, "RETOUR RAPIDE", rew)
        Media(Icons.Rounded.PlayArrow, "LECTURE / PAUSE", play)
        Media(Icons.Rounded.Stop, "STOP", stop)
        Media(Icons.Rounded.FastForward, "AVANCE RAPIDE", ff)
    }
}

@Composable
private fun Media(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    PremiumCircle(
        icon = icon,
        desc = label,
        size = MediaControlDimensions.buttonSize,
        onClick = onClick
    )
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
            AppTile(
                label = "NETFLIX",
                accent = AppColors.netflix,
                iconRes = R.drawable.ic_app_netflix,
                onClick = onLaunchNetflix,
                modifier = Modifier.weight(1f)
            )
            AppTile(
                label = "YOUTUBE",
                accent = AppColors.youtube,
                iconRes = R.drawable.ic_app_youtube,
                onClick = onLaunchYouTube,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HomeFooterDimensions.rowSpacing)
        ) {
            AppTile(
                label = "PLEX",
                accent = AppColors.plex,
                iconRes = R.drawable.ic_app_plex,
                onClick = onLaunchPlex,
                modifier = Modifier.weight(1f)
            )
            AppTile(
                label = "CRUNCHYROLL",
                accent = AppColors.crunchyroll,
                iconRes = R.drawable.ic_app_crunchy,
                onClick = onLaunchCrunchyroll,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AppTile(
    label: String,
    accent: Color,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTileDimensions.cornerRadius)

    Row(
        modifier
            .height(AppTileDimensions.height)
            .outerRoundedShadow(
                cornerRadius = AppTileDimensions.cornerRadius,
                color = Color.Black,
                alpha = 0.9f,
                blurRadius = AppTileDimensions.shadowBlurRadius,
                offsetY = AppTileDimensions.shadowOffsetY
            )
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to AppColors.remoteButtonTop,
                        0.35f to AppColors.remoteButtonBottom
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 500f)
                ),
                shape
            )
            .border(
                width = AppTileDimensions.mainBorderWidth,
                color = accent.copy(alpha = .58f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = AppTileDimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(AppTileDimensions.logoSize)
        )
    }
}
