package com.telecommande.ui.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.telecommande.ui.theme.DefaultButtonColors
import kotlin.math.roundToInt

private val Cyan = Color(0xFF19D7FF)
private val Deep = Color(0xFF070B10)
private val Surface = Color(0xFF111820)
private val Rim = Color(0xFF344451)

@Composable
fun HeaderSection(title: String, modifier: Modifier = Modifier, onPowerClick: () -> Unit, isConnected: Boolean, isLoading: Boolean, onStatusIndicatorClick: () -> Unit) {
    Box(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        PremiumCircle(Icons.Rounded.PowerSettingsNew, "Power", 58.dp, onPowerClick, Modifier.align(Alignment.CenterStart), if (isConnected) Color(0xFFFF4B55) else Color.White)
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
fun StatusIndicator(isConnected: Boolean, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, connectedIconRes: Int = 0, disconnectedIconRes: Int = 0) {
    Box(modifier.size(52.dp).background(Surface, CircleShape).border(1.dp, if (isConnected) Color(0xFF40E081) else Rim, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = Cyan)
        else Icon(if (isConnected) Icons.Rounded.Tv else Icons.Rounded.TvOff, if (isConnected) "Connectée" else "Déconnectée", tint = if (isConnected) Color(0xFF40E081) else Color.Gray, modifier = Modifier.size(27.dp))
    }
}

@Composable
fun ContentSection(modifier: Modifier = Modifier, onOkClick: () -> Unit, onUpClick: () -> Unit, onDownClick: () -> Unit, onLeftClick: () -> Unit, onRightClick: () -> Unit, onBackClick: () -> Unit, onHomeClick: () -> Unit, volumeLevel: Int, volumeMax: Int, isMuted: Boolean, onVolumeUpClick: () -> Unit, onVolumeDownClick: () -> Unit, onMuteClick: () -> Unit, onRewindClick: () -> Unit, onPlayPauseClick: () -> Unit, onStopClick: () -> Unit, onFastForwardClick: () -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compact = maxWidth < 360.dp
        val dpad = (maxWidth * .78f).coerceIn(240.dp, 310.dp)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            ConstraintLayout(Modifier.size(dpad).shadow(18.dp, CircleShape).background(Brush.radialGradient(listOf(Color(0xFF1B2630), Deep)), CircleShape).border(2.dp, Rim, CircleShape)) {
                val (ok,u,d,l,r)=createRefs()
                PremiumCircle(Icons.Rounded.Check, "OK", dpad*.31f, onOkClick, Modifier.constrainAs(ok){centerTo(parent)}, Cyan)
                DpadIcon(Icons.Rounded.KeyboardArrowUp,"Haut",dpad*.32f,onUpClick,Modifier.constrainAs(u){top.linkTo(parent.top,2.dp);start.linkTo(parent.start);end.linkTo(parent.end)})
                DpadIcon(Icons.Rounded.KeyboardArrowDown,"Bas",dpad*.32f,onDownClick,Modifier.constrainAs(d){bottom.linkTo(parent.bottom,2.dp);start.linkTo(parent.start);end.linkTo(parent.end)})
                DpadIcon(Icons.Rounded.KeyboardArrowLeft,"Gauche",dpad*.32f,onLeftClick,Modifier.constrainAs(l){start.linkTo(parent.start,2.dp);top.linkTo(parent.top);bottom.linkTo(parent.bottom)})
                DpadIcon(Icons.Rounded.KeyboardArrowRight,"Droite",dpad*.32f,onRightClick,Modifier.constrainAs(r){end.linkTo(parent.end,2.dp);top.linkTo(parent.top);bottom.linkTo(parent.bottom)})
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(44.dp)) {
                NavPill(Icons.AutoMirrored.Rounded.ArrowBack,"RETOUR",onBackClick)
                NavPill(Icons.Rounded.Home,"ACCUEIL",onHomeClick)
            }
            Spacer(Modifier.height(10.dp))
            VolumeControl(volumeLevel,volumeMax,isMuted,onVolumeUpClick,onVolumeDownClick,onMuteClick,compact)
            Spacer(Modifier.height(8.dp))
            MediaControls(onRewindClick,onPlayPauseClick,onStopClick,onFastForwardClick,compact)
        }
    }
}

@Composable private fun DpadIcon(icon: ImageVector, desc:String,size:Dp,onClick:()->Unit,modifier:Modifier=Modifier){
    IconButton(onClick = onClick, modifier = modifier.size(size)){
        Icon(icon,desc,tint=Color.White,modifier=Modifier.size(size*.50f))
    }
}

@Composable private fun PremiumCircle(icon:ImageVector,desc:String,size:Dp,onClick:()->Unit,modifier:Modifier=Modifier,tint:Color=Color.White){
    Box(modifier.size(size).shadow(10.dp,CircleShape).background(Brush.radialGradient(listOf(Color(0xFF1A232C),Deep)),CircleShape).border(1.dp,Rim,CircleShape).clickable(onClick=onClick),contentAlignment=Alignment.Center){Icon(icon,desc,tint=tint,modifier=Modifier.size(size*.48f))}
}

@Composable private fun NavPill(icon:ImageVector,label:String,onClick:()->Unit){
    Column(horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.width(92.dp).height(52.dp).shadow(8.dp,RoundedCornerShape(26.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF151E27),Deep)),RoundedCornerShape(26.dp)).border(1.dp,Rim,RoundedCornerShape(26.dp)).clickable(onClick=onClick),contentAlignment=Alignment.Center){Icon(icon,label,tint=Color.White,modifier=Modifier.size(26.dp))};Spacer(Modifier.height(4.dp));Text(label,color=Color(0xFFB9C4CC),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)}
}

@Composable private fun VolumeControl(volumeLevel:Int,volumeMax:Int,isMuted:Boolean,onVolumeUpClick:()->Unit,onVolumeDownClick:()->Unit,onMuteClick:()->Unit,compact:Boolean){
    val max=volumeMax.takeIf{it>0}?:100
    var pos by remember(volumeLevel){mutableStateOf(volumeLevel.toFloat())}
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f).padding(start=4.dp,end=10.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                Text("VOLUME",color=Color(0xFF9AA7B1),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
                Text("$volumeLevel",color=Cyan,fontWeight=FontWeight.Bold)
            }
            Slider(value=pos,onValueChange={pos=it},onValueChangeFinished={val target=pos.roundToInt().coerceIn(0,max);val diff=target-volumeLevel;if(diff>0)repeat(diff){onVolumeUpClick()}else if(diff<0)repeat(-diff){onVolumeDownClick()}},valueRange=0f..max.toFloat(),steps=if(max>0)max-1 else 0,colors=SliderDefaults.colors(thumbColor=Color.White,activeTrackColor=Cyan,inactiveTrackColor=DefaultButtonColors.DefaultBackgroundStart,activeTickColor=Color.Transparent,inactiveTickColor=Color.Transparent))
        }
        PremiumCircle(if(isMuted)Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,"Muet",48.dp,onMuteClick)
    }
}

@Composable private fun MediaControls(rew:()->Unit,play:()->Unit,stop:()->Unit,ff:()->Unit,compact:Boolean){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
        Media(Icons.Rounded.FastRewind,"RETOUR RAPIDE",rew)
        Media(Icons.Rounded.PlayArrow,"LECTURE / PAUSE",play)
        Media(Icons.Rounded.Stop,"STOP",stop)
        Media(Icons.Rounded.FastForward,"AVANCE RAPIDE",ff)
    }
}
@Composable private fun Media(icon:ImageVector,label:String,onClick:()->Unit){
    Column(horizontalAlignment=Alignment.CenterHorizontally){
        PremiumCircle(icon,label,62.dp,onClick,tint=Color.White)
        Spacer(Modifier.height(4.dp))
        Text(label,color=Color(0xFF9AA7B1),style=MaterialTheme.typography.labelSmall,textAlign=TextAlign.Center,maxLines=1)
    }
}

@Composable
fun FooterSection(modifier:Modifier=Modifier,onLaunchNetflix:()->Unit,onLaunchYouTube:()->Unit,onLaunchPlex:()->Unit,onLaunchCrunchyroll:()->Unit){
    Column(modifier.fillMaxWidth().padding(top=10.dp,bottom=10.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){AppTile("NETFLIX",Color(0xFFE50914),Icons.Rounded.Movie,onLaunchNetflix,Modifier.weight(1f));AppTile("YOUTUBE",Color(0xFFFF0033),Icons.Rounded.PlayCircle,onLaunchYouTube,Modifier.weight(1f))};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){AppTile("PLEX",Color(0xFFE5A900),Icons.Rounded.VideoLibrary,onLaunchPlex,Modifier.weight(1f));AppTile("CRUNCHYROLL",Color(0xFFFF7A00),Icons.Rounded.LiveTv,onLaunchCrunchyroll,Modifier.weight(1f))}}
}
@Composable private fun AppTile(label:String,accent:Color,icon:ImageVector,onClick:()->Unit,modifier:Modifier=Modifier){Row(modifier.height(62.dp).shadow(7.dp,RoundedCornerShape(16.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF121820),Deep)),RoundedCornerShape(16.dp)).border(1.dp,accent.copy(alpha=.75f),RoundedCornerShape(16.dp)).clickable(onClick=onClick),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center){Icon(icon,label,tint=accent,modifier=Modifier.size(27.dp));Spacer(Modifier.width(8.dp));Text(label,color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelLarge)}}