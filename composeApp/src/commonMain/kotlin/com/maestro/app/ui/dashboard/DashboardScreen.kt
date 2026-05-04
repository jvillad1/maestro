package com.maestro.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.shared.model.Task

@Composable
fun DashboardScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { DashboardViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(8000)
            vm.nextPhrase()
        }
    }

    AppScaffold(Screen.Dashboard.route, navController, pageTitle = "") {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaestroColors.Gold)
            }
            return@AppScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // KPI strip
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                KpiCard(
                    "Estudiantes activos", state.students.size.toString(),
                    "+${maxOf(0, state.students.size - 30)} este mes", MaestroColors.Espresso, Modifier.weight(1f)
                )
                KpiCard(
                    "Clases registradas", state.recentClasses.size.toString(),
                    "Este mes", MaestroColors.Terra, Modifier.weight(1f)
                )
                KpiCard(
                    "Por cobrar", dashFormatCOP(state.pendingIncome),
                    "${state.students.count { s -> state.recentClasses.any { it.studentId == s.id && !it.paid } }} pagos pendientes",
                    MaestroColors.Gold, Modifier.weight(1f)
                )
                KpiCard(
                    "Promedio asistencia", "94%",
                    "Últimos 30 días", MaestroColors.Terra, Modifier.weight(1f)
                )
            }

            // Two-column body
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                // Left column — 2fr
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Recent classes
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashSectionHeader("Clases recientes", "Ver todas →") {
                            navController.navigate(Screen.Classes.route)
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            if (state.recentClasses.isEmpty()) {
                                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                                    Text("Sin clases registradas", color = MaestroColors.Muted, fontSize = 13.sp)
                                }
                            } else {
                                Column {
                                    state.recentClasses.forEachIndexed { idx, cls ->
                                        val student = state.students.find { it.id == cls.studentId }
                                        DashScheduleRow(
                                            date = cls.date,
                                            studentName = student?.name ?: "Desconocido",
                                            studentColor = dashParseColor(student?.color ?: "#C9A84C"),
                                            detail = cls.topic,
                                            isPaid = cls.paid,
                                            isLast = idx == state.recentClasses.size - 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Pending tasks
                    if (state.pendingTasks.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashSectionHeader("Tareas pendientes", "Ver todas →") {
                                navController.navigate(Screen.Tasks.route)
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column {
                                    state.pendingTasks.forEachIndexed { idx, task ->
                                        DashTaskRow(task, isLast = idx == state.pendingTasks.size - 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Right column — 1fr
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Upcoming events
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashSectionHeader("Próximos eventos", null) {}
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            if (state.upcomingEvents.isEmpty()) {
                                Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                    Text("Sin eventos próximos", color = MaestroColors.Muted, fontSize = 13.sp)
                                }
                            } else {
                                Column {
                                    state.upcomingEvents.forEachIndexed { idx, event ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier.size(36.dp).background(MaestroColors.LightGold, RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) { Text("📅", fontSize = 16.sp) }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(event.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso)
                                                Text(event.date, fontSize = 11.sp, color = MaestroColors.Muted)
                                            }
                                        }
                                        if (idx < state.upcomingEvents.size - 1)
                                            HorizontalDivider(color = MaestroColors.LightGold)
                                    }
                                }
                            }
                        }
                    }

                    // Motivational phrase
                    val phrase = PHRASES[state.currentPhraseIndex]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashSectionHeader("Nota del día", null) {}
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF8E7), RoundedCornerShape(12.dp))
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    "\"${phrase.text}\"",
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaestroColors.Espresso,
                                    lineHeight = 21.sp
                                )
                                Text("— ${phrase.author}", fontSize = 11.sp, color = MaestroColors.Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, hint: String, accent: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Muted, letterSpacing = 0.5.sp)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaestroColors.Espresso, lineHeight = 30.sp)
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(hint, fontSize = 10.sp, color = accent, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun DashSectionHeader(title: String, actionLabel: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso, letterSpacing = 0.3.sp)
        if (actionLabel != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text(actionLabel, fontSize = 11.sp, color = MaestroColors.Terra)
            }
        }
    }
}

@Composable
private fun DashScheduleRow(
    date: String, studentName: String, studentColor: Color,
    detail: String, isPaid: Boolean, isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!isPaid) Color(0xFFFFF9F0) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(date.takeLast(5), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaestroColors.Muted,
            modifier = Modifier.width(46.dp))
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(studentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(studentName.firstOrNull()?.uppercase() ?: "?", color = studentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(studentName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso)
            Text(detail, fontSize = 11.sp, color = MaestroColors.Muted)
        }
        Box(
            modifier = Modifier
                .background(
                    if (isPaid) MaestroColors.SoftGreen else Color(0xFFFDE8D8),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                if (isPaid) "Pagado" else "Pendiente",
                fontSize = 10.sp,
                color = if (isPaid) MaestroColors.Forest else MaestroColors.Terra,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    if (!isLast) HorizontalDivider(color = MaestroColors.LightGold)
}

@Composable
private fun DashTaskRow(task: Task, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.Transparent, RoundedCornerShape(3.dp))
                .padding(1.dp)
                .background(MaestroColors.LightGold, RoundedCornerShape(3.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(task.text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaestroColors.Espresso)
        }
        Box(
            modifier = Modifier
                .background(
                    when (task.priority.name) {
                        "ALTA" -> Color(0xFFFDE8D8)
                        "MEDIA" -> MaestroColors.LightGold
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                when (task.priority.name) {
                    "ALTA" -> "Alta"
                    "MEDIA" -> "Media"
                    else -> ""
                },
                fontSize = 10.sp,
                color = when (task.priority.name) {
                    "ALTA" -> MaestroColors.Terra
                    "MEDIA" -> MaestroColors.Muted
                    else -> Color.Transparent
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
    if (!isLast) HorizontalDivider(color = MaestroColors.LightGold)
}

private fun dashParseColor(hex: String): Color {
    val clean = hex.trimStart('#')
    if (clean.length < 6) return MaestroColors.Gold
    val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
    val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
    val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
    return Color(r / 255f, g / 255f, b / 255f)
}

private fun dashFormatCOP(amount: Long): String =
    "$${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"
