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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.R
import com.telecommande.ui.theme.AppColors
import com.telecommande.util.outerRoundedShadow
import com.telecommande.util.outerShadow
import kotlin.math.roundToInt

@Composable
fun HeaderSection(title: String, modifier: Modifier = Modifier, onPowerClick: () -> Unit, isConnected: Boolean, isLoading: Boolean, onStatusIndicatorClick: () -> Unit) {
    Box(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        PremiumCircle(
            icon = Icons.Rounded.PowerSettingsNew,
            desc = "Power",
            size = 58.dp,
            onClick = onPowerClick,
            modifier = Modifier.align(Alignment.CenterStart),
            iconTint = if (isConnected) AppColors.statusRed else AppColors.statusGreen
        )
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = AppColors.appWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(if (isConnected) AppColors.remoteConnected else AppColors.statusGray, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isConnected) "TV CONNECTÉE" else "TV DÉCONNECTÉE",
                    color = if (isConnected) AppColors.remoteConnected else AppColors.statusGray,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        StatusIndicator(isConnected, isLoading, onStatusIndicatorClick, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun StatusIndicator(isConnected: Boolean, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = when { isLoading -> "Connexion en cours"; isConnected -> "Connectée"; else -> "Déconnectée" }
    val statusTint = when { isLoading -> AppColors.statusAmber; isConnected -> AppColors.statusGreen; else -> AppColors.statusGray }
    PremiumCircle(Icons.Rounded.Tv, description, 58.dp, onClick, modifier, .58f, statusTint)
}

@Composable
fun ContentSection(modifier: Modifier = Modifier, onOkClick: () -> Unit, onUpClick: () -> Unit, onDownClick: () -> Unit, onLeftClick: () -> Unit, onRightClick: () -> Unit, onBackClick: () -> Unit, onHomeClick: () -> Unit, volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit, onRewindClick: () -> Unit, onPlayPauseClick: () -> Unit, onStopClick: () -> Unit, onFastForwardClick: () -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val dpad = (maxWidth * .78f).coerceIn(240.dp, 310.dp)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            ConstraintLayout(Modifier.size(dpad).shadow(18.dp, CircleShape).background(Brush.radialGradient(0.0f to AppColors.dpadCenter, 0.58f to AppColors.dpadCenter, 0.82f to AppColors.dpadMid, 1.0f to AppColors.dpadEdge), CircleShape).border(2.dp, AppColors.remoteRim, CircleShape)) {
                val (ok, u, d, l, r) = createRefs()
                PremiumCircle(Icons.Rounded.Check, "OK", dpad * .31f, onOkClick, Modifier.constrainAs(ok) { centerTo(parent) }, iconScale = .62f)
                DpadIcon(Icons.Rounded.ArrowDropDown, "Haut", dpad * .40f, onUpClick, Modifier.constrainAs(u) { top.linkTo(parent.top); start.linkTo(parent.start); end.linkTo(parent.end) }, 180f)
                DpadIcon(Icons.Rounded.ArrowDropDown, "Bas", dpad * .40f, onDownClick, Modifier.constrainAs(d) { bottom.linkTo(parent.bottom); start.linkTo(parent.start); end.linkTo(parent.end) }, 0f)
                DpadIcon(Icons.Rounded.ArrowDropDown, "Gauche", dpad * .40f, onLeftClick, Modifier.constrainAs(l) { start.linkTo(parent.start); top.linkTo(parent.top); bottom.linkTo(parent.bottom) }, 90f)
                DpadIcon(Icons.Rounded.ArrowDropDown, "Droite", dpad * .40f, onRightClick, Modifier.constrainAs(r) { end.linkTo(parent.end); top.linkTo(parent.top); bottom.linkTo(parent.bottom) }, -90f)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                NavPill(Icons.Rounded.ArrowBackIosNew, "RETOUR", onBackClick)
                NavPill(Icons.Rounded.Home, "ACCUEIL", onHomeClick)
            }
            Spacer(Modifier.height(10.dp))
            VolumeControl(volumeLevel, volumeMax, isMuted, onVolumeUpClick, onVolumeDownClick, onMuteClick)
            Spacer(Modifier.height(22.dp))
            MediaControls(onRewindClick, onPlayPauseClick, onStopClick, onFastForwardClick)
        }
    }
}

@Composable
private fun GradientIcon(icon: ImageVector, desc: String, modifier: Modifier = Modifier, tint: Color? = null, brush: Brush = Brush.verticalGradient(listOf(AppColors.appWhite, AppColors.textSecondary))) {
    if (tint != null) {
        Icon(imageVector = icon, contentDescription = desc, tint = tint, modifier = modifier)
    } else {
        Icon(imageVector = icon, contentDescription = desc, tint = Color.White, modifier = modifier.graphicsLayer(alpha = 0.99f).drawWithCache { onDrawWithContent { drawContent(); drawRect(brush = brush, blendMode = BlendMode.SrcAtop) } })
    }
}

@Composable
private fun DpadIcon(icon: ImageVector, desc: String, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier, rotation: Float = 0f) {
    IconButton(onClick = onClick, modifier = modifier.size(size)) {
        GradientIcon(icon = icon, desc = desc, modifier = Modifier.size(size * .62f).graphicsLayer(rotationZ = rotation))
    }
}

@Composable
private fun PremiumCircle(icon: ImageVector, desc: String, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier, iconScale: Float = .60f, iconTint: Color? = null) {
    Box(modifier.size(size).outerShadow(color = Color.Black, alpha = 0.9f, blurRadius = 5.dp, offsetY = 3.dp).background(Brush.linearGradient(colorStops = arrayOf(0.0f to AppColors.remoteButtonTop, 0.35f to AppColors.remoteButtonBottom), start = Offset(0f, 0f), end = Offset(0f, 500f)), CircleShape).border(width = 1.dp, brush = Brush.linearGradient(colorStops = arrayOf(0.0f to AppColors.remoteButtonBorderTop, 0.15f to AppColors.remoteButtonBorderBottom), start = Offset(0f, 0f), end = Offset(0f, 500f)), shape = CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().border(1.dp, Color.Black, CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
            GradientIcon(icon, desc, Modifier.size(size * iconScale), iconTint)
        }
    }
}

@Composable
private fun NavPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(92.dp).height(52.dp).outerRoundedShadow(26.dp, Color.Black, 0.9f, 5.dp, 3.dp).background(Brush.linearGradient(colorStops = arrayOf(0.0f to AppColors.remoteButtonTop, 0.35f to AppColors.remoteButtonBottom), start = Offset(0f, 0f), end = Offset(0f, 500f)), shape).border(width = 1.dp, brush = Brush.linearGradient(colorStops = arrayOf(0.0f to AppColors.remoteButtonBorderTop, 0.15f to AppColors.remoteButtonBorderBottom), start = Offset(0f, 0f), end = Offset(0f, 500f)), shape = shape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxSize().border(1.dp, Color.Black, shape).clip(shape), contentAlignment = Alignment.Center) {
                GradientIcon(icon, label, Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = AppColors.textSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VolumeControl(volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit) {
    val max = volumeMax.takeIf { it > 0 } ?: 100
    var pos by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }
    val sliderColors = SliderDefaults.colors(activeTrackColor = AppColors.remoteCyan, inactiveTrackColor = AppColors.volumeTrackInactive, activeTickColor = AppColors.transparent, inactiveTickColor = AppColors.transparent)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(start = 4.dp, end = 10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("VOLUME", color = AppColors.remoteTextMuted, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("$volumeLevel", color = AppColors.remoteCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(3.dp))
            Box(Modifier.fillMaxWidth().height(38.dp).shadow(7.dp, RoundedCornerShape(19.dp)).background(Brush.horizontalGradient(listOf(AppColors.volumePanelStart, AppColors.volumePanelEnd)), RoundedCornerShape(19.dp)).border(1.dp, AppColors.remoteRim, RoundedCornerShape(19.dp)).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                Slider(value = pos, onValueChange = { pos = it }, onValueChangeFinished = { val target = pos.roundToInt().coerceIn(0, max); val diff = target - volumeLevel; if (diff > 0) repeat(diff) { onVolumeUpClick() } else if (diff < 0) repeat(-diff) { onVolumeDownClick() } }, valueRange = 0f..max.toFloat(), steps = 0, modifier = Modifier.fillMaxWidth(), colors = sliderColors, thumb = { Box(Modifier.size(20.dp).shadow(4.dp, CircleShape).background(Brush.verticalGradient(listOf(AppColors.volumeThumbTop, AppColors.volumeThumbBottom)), CircleShape).border(1.5.dp, AppColors.volumeThumbBorder, CircleShape)) }, track = { sliderState -> SliderDefaults.Track(sliderState = sliderState, colors = sliderColors, thumbTrackGapSize = 0.dp, trackInsideCornerSize = 0.dp) })
            }
        }
        PremiumCircle(icon = if (isMuted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp, desc = "Muet", size = 48.dp, onClick = onMuteClick, modifier = Modifier.padding(top = 20.dp))
    }
}

@Composable
private fun MediaControls(rew: () -> Unit, play: () -> Unit, stop: () -> Unit, ff: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Media(Icons.Rounded.FastRewind, "RETOUR RAPIDE", rew)
        Media(Icons.Rounded.PlayArrow, "LECTURE / PAUSE", play)
        Media(Icons.Rounded.Stop, "STOP", stop)
        Media(Icons.Rounded.FastForward, "AVANCE RAPIDE", ff)
    }
}

@Composable
private fun Media(icon: ImageVector, label: String, onClick: () -> Unit) { PremiumCircle(icon, label, 62.dp, onClick) }

@Composable
fun FooterSection(modifier: Modifier = Modifier, onLaunchNetflix: () -> Unit, onLaunchYouTube: () -> Unit, onLaunchPlex: () -> Unit, onLaunchCrunchyroll: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AppTile("NETFLIX", AppColors.netflix, R.drawable.ic_app_netflix, onLaunchNetflix, Modifier.weight(1f))
            AppTile("YOUTUBE", AppColors.youtube, R.drawable.ic_app_youtube, onLaunchYouTube, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AppTile("PLEX", AppColors.plex, R.drawable.ic_app_plex, onLaunchPlex, Modifier.weight(1f))
            AppTile("CRUNCHYROLL", AppColors.crunchyroll, R.drawable.ic_app_crunchy, onLaunchCrunchyroll, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AppTile(label: String, accent: Color, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.height(66.dp).shadow(12.dp, RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(AppColors.surfacePressed, AppColors.surface, AppColors.remoteDeep)), RoundedCornerShape(17.dp)).border(1.dp, accent.copy(alpha = .48f), RoundedCornerShape(17.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Image(painter = painterResource(iconRes), contentDescription = label, modifier = Modifier.size(100.dp))
    }
}
