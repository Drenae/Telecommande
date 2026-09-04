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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.R
import kotlin.math.roundToInt

private val Cyan = Color(0xFF19D7FF)
private val Deep = Color(0xFF070B10)
private val Rim = Color(0xFF263540)
private val ButtonRim = Color(0xFF33434F)
private val RaisedTop = Color(0xFF25333E)
private val RaisedMid = Color(0xFF151E26)
private val RaisedBottom = Color(0xFF090D12)

@Composable
fun HeaderSection(title: String, modifier: Modifier = Modifier, onPowerClick: () -> Unit, isConnected: Boolean, isLoading: Boolean, onStatusIndicatorClick: () -> Unit) {
    Box(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        PremiumCircle(
            iconRes = if (isConnected) R.drawable.power_off else R.drawable.power_up,
            desc = "Power",
            size = 58.dp,
            onClick = onPowerClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(if (isConnected) Color(0xFF40E081) else Color.Gray, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(if (isConnected) "TV CONNECTÉE" else "TV DÉCONNECTÉE", color = if (isConnected) Color(0xFF40E081) else Color.Gray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        StatusIndicator(isConnected, isLoading, onStatusIndicatorClick, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
fun StatusIndicator(
    isConnected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes connectedIconRes: Int = R.drawable.status_ok,
    @DrawableRes disconnectedIconRes: Int = R.drawable.status_unknow
) {
    val statusIcon = when {
        isLoading -> R.drawable.status_warning
        isConnected -> connectedIconRes
        else -> disconnectedIconRes
    }
    val description = when {
        isLoading -> "Connexion en cours"
        isConnected -> "Connectée"
        else -> "Déconnectée"
    }

    Box(
        modifier
            .size(58.dp)
            .shadow(12.dp, CircleShape)
            .background(Brush.linearGradient(listOf(RaisedTop, RaisedMid, RaisedBottom)), CircleShape)
            .border(1.dp, if (isConnected) Color(0xFF208F5B) else ButtonRim, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(statusIcon),
            contentDescription = description,
            modifier = Modifier.size(46.dp)
        )
    }
}

@Composable
fun ContentSection(modifier: Modifier = Modifier, onOkClick: () -> Unit, onUpClick: () -> Unit, onDownClick: () -> Unit, onLeftClick: () -> Unit, onRightClick: () -> Unit, onBackClick: () -> Unit, onHomeClick: () -> Unit, volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit, onRewindClick: () -> Unit, onPlayPauseClick: () -> Unit, onStopClick: () -> Unit, onFastForwardClick: () -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val dpad = (maxWidth * .78f).coerceIn(240.dp, 310.dp)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            ConstraintLayout(Modifier.size(dpad).shadow(18.dp, CircleShape).background(Brush.radialGradient(listOf(Color(0xFF1B2630), Deep)), CircleShape).border(2.dp, Rim, CircleShape)) {
                val (ok, u, d, l, r) = createRefs()
                PremiumCircle(R.drawable.ok, "OK", dpad * .31f, onOkClick, Modifier.constrainAs(ok) { centerTo(parent) })
                DpadIcon(R.drawable.dpad_up, "Haut", dpad * .32f, onUpClick, Modifier.constrainAs(u) { top.linkTo(parent.top, 2.dp); start.linkTo(parent.start); end.linkTo(parent.end) })
                DpadIcon(R.drawable.dpad_down, "Bas", dpad * .32f, onDownClick, Modifier.constrainAs(d) { bottom.linkTo(parent.bottom, 2.dp); start.linkTo(parent.start); end.linkTo(parent.end) })
                DpadIcon(R.drawable.dpad_left, "Gauche", dpad * .32f, onLeftClick, Modifier.constrainAs(l) { start.linkTo(parent.start, 2.dp); top.linkTo(parent.top); bottom.linkTo(parent.bottom) })
                DpadIcon(R.drawable.dpad_right, "Droite", dpad * .32f, onRightClick, Modifier.constrainAs(r) { end.linkTo(parent.end, 2.dp); top.linkTo(parent.top); bottom.linkTo(parent.bottom) })
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                NavPill(R.drawable.back, "RETOUR", onBackClick)
                NavPill(R.drawable.home, "ACCUEIL", onHomeClick)
            }
            Spacer(Modifier.height(10.dp))
            VolumeControl(volumeLevel, volumeMax, isMuted, onVolumeUpClick, onVolumeDownClick, onMuteClick)
            Spacer(Modifier.height(22.dp))
            MediaControls(onRewindClick, onPlayPauseClick, onStopClick, onFastForwardClick)
        }
    }
}

@Composable
private fun DpadIcon(@DrawableRes iconRes: Int, desc: String, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(size)) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = desc,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PremiumCircle(@DrawableRes iconRes: Int, desc: String, size: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .shadow(14.dp, CircleShape)
            .background(Brush.linearGradient(listOf(RaisedTop, RaisedMid, RaisedBottom)), CircleShape)
            .border(1.dp, ButtonRim, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = desc,
            modifier = Modifier.size(size * .70f)
        )
    }
}

@Composable
private fun NavPill(@DrawableRes iconRes: Int, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(92.dp)
                .height(52.dp)
                .shadow(12.dp, RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(RaisedTop, RaisedMid, RaisedBottom)), RoundedCornerShape(26.dp))
                .border(1.dp, ButtonRim, RoundedCornerShape(26.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(46.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color(0xFFB9C4CC), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VolumeControl(volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit) {
    val max = volumeMax.takeIf { it > 0 } ?: 100
    var pos by remember(volumeLevel) { mutableStateOf(volumeLevel.toFloat()) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(start = 4.dp, end = 10.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("VOLUME", color = Color(0xFF9AA7B1), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text("$volumeLevel", color = Cyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(3.dp))
            Box(Modifier.fillMaxWidth().height(38.dp).shadow(7.dp, RoundedCornerShape(19.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF0D141B), Color(0xFF111C24))), RoundedCornerShape(19.dp)).border(1.dp, Rim, RoundedCornerShape(19.dp)).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                Slider(
                    value = pos,
                    onValueChange = { pos = it },
                    onValueChangeFinished = {
                        val target = pos.roundToInt().coerceIn(0, max)
                        val diff = target - volumeLevel
                        if (diff > 0) repeat(diff) { onVolumeUpClick() }
                        else if (diff < 0) repeat(-diff) { onVolumeDownClick() }
                    },
                    valueRange = 0f..max.toFloat(),
                    steps = 0,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(activeTrackColor = Cyan, inactiveTrackColor = Color(0xFF24323D), activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent),
                    thumb = { Box(Modifier.size(22.dp).shadow(7.dp, CircleShape).background(Brush.radialGradient(listOf(Color.White, Cyan)), CircleShape).border(2.dp, Color(0xFF6CC9DA), CircleShape)) }
                )
            }
        }
        PremiumCircle(
            iconRes = if (isMuted) R.drawable.mute else R.drawable.volume,
            desc = "Muet",
            size = 48.dp,
            onClick = onMuteClick,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun MediaControls(rew: () -> Unit, play: () -> Unit, stop: () -> Unit, ff: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Media(R.drawable.fast_rewind, "RETOUR RAPIDE", rew)
        Media(R.drawable.play, "LECTURE / PAUSE", play)
        Media(R.drawable.stop, "STOP", stop)
        Media(R.drawable.fast_forward, "AVANCE RAPIDE", ff)
    }
}

@Composable
private fun Media(@DrawableRes iconRes: Int, label: String, onClick: () -> Unit) {
    PremiumCircle(iconRes, label, 62.dp, onClick)
}

@Composable
fun FooterSection(modifier: Modifier = Modifier, onLaunchNetflix: () -> Unit, onLaunchYouTube: () -> Unit, onLaunchPlex: () -> Unit, onLaunchCrunchyroll: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AppTile("NETFLIX", Color(0xFFE50914), R.drawable.ic_app_netflix, onLaunchNetflix, Modifier.weight(1f))
            AppTile("YOUTUBE", Color(0xFFFF0033), R.drawable.ic_app_youtube, onLaunchYouTube, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            AppTile("PLEX", Color(0xFFE5A900), R.drawable.ic_app_plex, onLaunchPlex, Modifier.weight(1f))
            AppTile("CRUNCHYROLL", Color(0xFFFF7A00), R.drawable.ic_app_crunchy, onLaunchCrunchyroll, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AppTile(label: String, accent: Color, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(66.dp)
            .shadow(12.dp, RoundedCornerShape(17.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF202B34), Color(0xFF111820), Deep)), RoundedCornerShape(17.dp))
            .border(1.dp, accent.copy(alpha = .48f), RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(100.dp)
        )
    }
}
