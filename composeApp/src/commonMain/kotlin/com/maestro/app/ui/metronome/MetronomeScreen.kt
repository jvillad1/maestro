package com.maestro.app.ui.metronome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maestro.app.audio.AudioPlayer
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import kotlinx.coroutines.delay

@Composable
fun MetronomeScreen(navController: NavHostController) {
    var bpm by remember { mutableStateOf(80f) }
    var isPlaying by remember { mutableStateOf(false) }
    var beat by remember { mutableStateOf(0) }
    var timeSignature by remember { mutableStateOf(4) }
    var accent by remember { mutableStateOf(true) }
    val audioPlayer = remember { AudioPlayer() }

    LaunchedEffect(isPlaying, bpm, timeSignature, accent) {
        if (!isPlaying) { beat = 0; return@LaunchedEffect }
        while (isPlaying) {
            audioPlayer.playClick(accent && beat == 0)
            delay(60_000L / bpm.toLong())
            beat = (beat + 1) % timeSignature
        }
    }

    AppScaffold(Screen.Metronome.route, navController, pageTitle = "Metrónomo") {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Metrónomo", style = MaterialTheme.typography.headlineMedium, color = MaestroColors.Espresso)

            Card(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.Espresso),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Beat visualizer
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(timeSignature) { i ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp, if (i == beat && isPlaying) 64.dp else 40.dp)
                                    .background(
                                        if (i == beat && isPlaying)
                                            if (accent && i == 0) MaestroColors.Gold else MaestroColors.Terra
                                        else MaestroColors.Muted.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    Text("${bpm.toInt()}", color = MaestroColors.White, fontSize = 72.sp)
                    Text("BPM", color = MaestroColors.Muted, style = MaterialTheme.typography.bodyMedium)

                    Slider(
                        value = bpm, onValueChange = { bpm = it },
                        valueRange = 20f..240f,
                        colors = SliderDefaults.colors(thumbColor = MaestroColors.Gold, activeTrackColor = MaestroColors.Gold)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { bpm = (bpm - 5).coerceAtLeast(20f) }) {
                            Text("−", color = MaestroColors.White, fontSize = 20.sp)
                        }
                        Button(
                            onClick = { isPlaying = !isPlaying },
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) MaestroColors.Terra else MaestroColors.Gold
                            )
                        ) {
                            Text(if (isPlaying) "⏹" else "▶", color = MaestroColors.Espresso, fontSize = 20.sp)
                        }
                        IconButton(onClick = { bpm = (bpm + 5).coerceAtMost(240f) }) {
                            Text("+", color = MaestroColors.White, fontSize = 20.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 4, 6).forEach { sig ->
                            Button(
                                onClick = { timeSignature = sig; beat = 0 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (timeSignature == sig) MaestroColors.Gold else MaestroColors.Muted.copy(alpha = 0.3f)
                                )
                            ) {
                                Text("$sig/4", color = MaestroColors.Espresso)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(
                            checked = accent, onCheckedChange = { accent = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaestroColors.Gold)
                        )
                        Text("Acentuar primer tiempo", color = MaestroColors.Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Tempo reference card
            Card(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Referencia de Tempos", style = MaterialTheme.typography.labelLarge, color = MaestroColors.Espresso)
                    val tempos = listOf("Largo" to 50, "Adagio" to 68, "Andante" to 92, "Moderato" to 114, "Allegro" to 138, "Presto" to 184)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        tempos.forEach { (name, targetBpm) ->
                            OutlinedButton(
                                onClick = { bpm = targetBpm.toFloat() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(name, fontSize = 10.sp, color = MaestroColors.Espresso)
                                    Text("$targetBpm", fontSize = 9.sp, color = MaestroColors.Muted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
