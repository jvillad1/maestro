package com.maestro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.maestro.app.notifications.DailyReminder
import com.maestro.app.theme.MaestroColors
import kotlinx.coroutines.launch

private fun two(n: Int) = if (n < 10) "0$n" else "$n"

/** Ajuste del recordatorio diario: encender/apagar y elegir la hora. */
@Composable
fun ReminderDialog(onDismiss: () -> Unit) {
    val reminder = remember { DailyReminder() }
    var enabled by remember { mutableStateOf(reminder.isEnabled()) }
    var hour by remember { mutableStateOf(reminder.hour()) }
    var minute by remember { mutableStateOf(reminder.minute()) }
    var denied by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Recordatorio diario", style = MaterialTheme.typography.titleMedium,
                    color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)
                Text("Un aviso cada día con las clases que tienes programadas.",
                    fontSize = 12.sp, color = MaestroColors.Muted)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activado", fontSize = 14.sp, color = MaestroColors.Espresso,
                        modifier = Modifier.weight(1f))
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it; denied = false },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaestroColors.Terra)
                    )
                }

                if (enabled) {
                    HorizontalDivider(color = MaestroColors.LightGold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hora", fontSize = 14.sp, color = MaestroColors.Espresso,
                            modifier = Modifier.weight(1f))
                        Stepper(label = two(hour), onLess = { hour = (hour + 23) % 24 }, onMore = { hour = (hour + 1) % 24 })
                        Text(":", fontSize = 16.sp, color = MaestroColors.Muted,
                            modifier = Modifier.padding(horizontal = 4.dp))
                        Stepper(label = two(minute), onLess = { minute = (minute + 45) % 60 }, onMore = { minute = (minute + 15) % 60 })
                    }
                }

                if (denied) {
                    Text("Android bloqueó las notificaciones de Maestro. Actívalas en los ajustes del teléfono y vuelve a intentar.",
                        fontSize = 12.sp, color = MaestroColors.Terra)
                }

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    OutlinedButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
                    Button(
                        onClick = {
                            saving = true
                            scope.launch {
                                if (enabled) {
                                    if (reminder.enable(hour, minute)) onDismiss() else { denied = true; saving = false }
                                } else {
                                    reminder.disable(); onDismiss()
                                }
                            }
                        },
                        enabled = !saving,
                        colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra)
                    ) { Text("Guardar") }
                }
            }
        }
    }
}

@Composable
private fun Stepper(label: String, onLess: () -> Unit, onMore: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepButton("−", onLess)
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso,
            modifier = Modifier.padding(horizontal = 10.dp))
        StepButton("+", onMore)
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(MaestroColors.LightGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxSize()) {
            Text(symbol, fontSize = 16.sp, color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)
        }
    }
}
