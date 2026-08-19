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
import com.maestro.shared.repository.MaestroRepository
import com.maestro.app.theme.MaestroColors
import com.maestro.app.theme.frauncesFamily
import com.maestro.app.ui.components.AppScaffold
import com.maestro.app.ui.components.LocalWindowWidthClass
import com.maestro.app.ui.components.WindowWidthClass
import com.maestro.app.ui.components.MaestroExpansiveHeader
import com.maestro.app.ui.components.formatHeaderDate
import com.maestro.app.ui.components.AttendanceChip
import com.maestro.shared.model.Attendance
import com.maestro.shared.model.Task

@Composable
fun DashboardScreen(
    repository: MaestroRepository,
    navController: NavHostController,
    userName: String = ""
) {
    val vm = viewModel { DashboardViewModel(repository) }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(8000)
            vm.nextPhrase()
        }
    }

    val today = remember { getCurrentDate() }
    val dateLabel = remember(today) { formatHeaderDate(today) }

    val subtitle = remember(state.recentClasses, state.pendingTasks) {
        if (!state.isLoading) {
            val pending = state.students.count { s ->
                state.recentClasses.any { it.studentId == s.id && !it.paid }
            }
            val classesCnt = state.recentClasses.size
            "Tienes $classesCnt clases registradas este mes" +
                if (pending > 0) " y $pending ${if (pending == 1) "pago" else "pagos"} por confirmar." else "."
        } else ""
    }

    AppScaffold(
        currentRoute = Screen.Dashboard.route,
        navController = navController,
        userName = userName,
        dashboardHeader = {
            MaestroExpansiveHeader(
                userName = userName,
                dateLabel = dateLabel,
                subtitle = subtitle,
                onNewClass = { navController.navigate(Screen.Classes.route) },
                onToday = { navController.navigate(Screen.Classes.route) }
            )
        }
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaestroColors.Gold)
            }
            return@AppScaffold
        }

        val compact = LocalWindowWidthClass.current == WindowWidthClass.Compact
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (compact) 16.dp else 28.dp, vertical = if (compact) 16.dp else 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // KPI strip — 4-up on desktop, 2x2 on phones
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                maxItemsInEachRow = if (compact) 2 else 4
            ) {
                KpiCard("ESTUDIANTES ACTIVOS", state.students.size.toString(), "+${maxOf(0, state.students.size - 30)} este mes", MaestroColors.Espresso, Modifier.weight(1f))
                KpiCard("CLASES ESTA SEMANA", state.recentClasses.size.toString(), "${state.recentClasses.size} este mes", MaestroColors.Terra, Modifier.weight(1f))
                KpiCard("POR COBRAR", dashFormatCOP(state.pendingIncome),
                    "${state.students.count { s -> state.recentClasses.any { it.studentId == s.id && !it.paid } }} pagos pendientes",
                    MaestroColors.Gold, Modifier.weight(1f))
                KpiCard(
                    "PROMEDIO ASISTENCIA",
                    state.attendanceRate?.let { "$it%" } ?: "—",
                    if (state.attendanceRate != null) "Este mes" else "Marca asistencia en Clases",
                    MaestroColors.Terra, Modifier.weight(1f)
                )
            }

            // Body — two columns on desktop, stacked on phones
            val leftSections: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Today's agenda — what the teacher opens the app for in the morning
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashSectionHeader(
                            "Clases de hoy" + if (state.todayClasses.isNotEmpty()) " · ${state.todayClasses.size}" else "",
                            "Ver agenda ›"
                        ) { navController.navigate(Screen.Classes.route) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            if (state.todayClasses.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("Sin clases para hoy", fontSize = 12.5.sp, color = MaestroColors.Espresso)
                                    Text("Registra una clase con la fecha de hoy para verla acá.",
                                        fontSize = 11.sp, color = MaestroColors.Muted)
                                }
                            } else {
                                Column {
                                    state.todayClasses.forEachIndexed { idx, cls ->
                                        val student = state.students.find { it.id == cls.studentId }
                                        DashTodayRow(
                                            studentName = student?.name ?: "Desconocido",
                                            studentColor = dashParseColor(student?.color ?: "#C9A84C"),
                                            topic = cls.topic,
                                            attendance = cls.attendance,
                                            onCycleAttendance = { vm.cycleAttendance(cls) },
                                            isLast = idx == state.todayClasses.size - 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recent classes
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashSectionHeader("Clases recientes", "Ver agenda completa ›") {
                            navController.navigate(Screen.Classes.route)
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            if (state.recentClasses.isEmpty()) {
                                Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                    Text("Sin clases este mes", fontSize = 12.sp, color = MaestroColors.Muted)
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
                            DashSectionHeader("Tareas pendientes", "Nueva tarea") {
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

            }

            val rightSections: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Próximos pagos
                    val unpaidStudents = state.students.filter { s ->
                        state.recentClasses.any { it.studentId == s.id && !it.paid }
                    }.take(4)

                    if (unpaidStudents.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashSectionHeader("Próximos pagos", "Ver todos") {
                                navController.navigate(Screen.Finances.route)
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    unpaidStudents.forEachIndexed { idx, student ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val color = dashParseColor(student.color)
                                            Box(
                                                modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(student.name.firstOrNull()?.uppercase() ?: "?",
                                                    color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(student.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso)
                                                Text("Mensualidad pendiente", fontSize = 11.sp, color = MaestroColors.Muted)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    dashFormatCOP(student.monthlyFee),
                                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                                    color = MaestroColors.Espresso
                                                )
                                                Box(
                                                    modifier = Modifier.background(Color(0xFFFDE8D8), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Pendiente", fontSize = 9.sp, color = MaestroColors.Terra, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                        if (idx < unpaidStudents.size - 1) HorizontalDivider(color = MaestroColors.LightGold)
                                    }
                                }
                            }
                        }
                    }

                    // Nota del día
                    val phrase = PHRASES[state.currentPhraseIndex]
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashSectionHeader("Notas rápidas", null) {}
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

                    // Upcoming events
                    if (state.upcomingEvents.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            DashSectionHeader("Próximos eventos", null) {}
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column {
                                    state.upcomingEvents.take(3).forEachIndexed { idx, event ->
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
                                        if (idx < state.upcomingEvents.size - 1) HorizontalDivider(color = MaestroColors.LightGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (compact) {
                leftSections()
                rightSections()
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(Modifier.weight(2f)) { leftSections() }
                    Box(Modifier.weight(1f)) { rightSections() }
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
        val compact = LocalWindowWidthClass.current == WindowWidthClass.Compact
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Muted, letterSpacing = 0.7.sp)
            Text(value, fontFamily = frauncesFamily(), fontSize = if (compact) 21.sp else 28.sp, fontWeight = FontWeight.SemiBold, color = accent, lineHeight = if (compact) 25.sp else 32.sp, maxLines = 1)
            Box(modifier = Modifier.background(accent.copy(alpha = 0.10f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(hint, fontSize = 10.sp, color = accent, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun DashSectionHeader(title: String, actionLabel: String?, onAction: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso, letterSpacing = 0.2.sp)
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
        Text(date.takeLast(5), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaestroColors.Espresso,
            modifier = Modifier.width(52.dp))
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(studentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center) {
            Text(studentName.firstOrNull()?.uppercase() ?: "?", color = studentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(studentName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso)
            Text(detail, fontSize = 11.sp, color = MaestroColors.Muted)
        }
        Box(modifier = Modifier
            .background(if (isPaid) MaestroColors.SoftGreen else Color(0xFFFDE8D8), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)) {
            Text(if (isPaid) "Pagado" else "Pendiente", fontSize = 10.sp,
                color = if (isPaid) MaestroColors.Forest else MaestroColors.Terra, fontWeight = FontWeight.SemiBold)
        }
    }
    if (!isLast) HorizontalDivider(color = MaestroColors.LightGold)
}

@Composable
private fun DashTaskRow(task: Task, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).background(MaestroColors.LightGold, RoundedCornerShape(3.dp)))
        Text(task.text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaestroColors.Espresso, modifier = Modifier.weight(1f))
        if (task.priority.name == "ALTA") {
            Box(modifier = Modifier.background(Color(0xFFFDE8D8), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("Prioridad", fontSize = 10.sp, color = MaestroColors.Terra, fontWeight = FontWeight.SemiBold)
            }
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
    "\$${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"

@Composable
private fun DashTodayRow(
    studentName: String,
    studentColor: Color,
    topic: String,
    attendance: Attendance,
    onCycleAttendance: () -> Unit,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(studentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(studentName.firstOrNull()?.uppercase() ?: "?",
                color = studentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(studentName, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold,
                color = MaestroColors.Espresso, maxLines = 1)
            Text(topic, fontSize = 11.sp, color = MaestroColors.Muted, maxLines = 1)
        }
        AttendanceChip(attendance = attendance, onClick = onCycleAttendance)
    }
    if (!isLast) HorizontalDivider(color = MaestroColors.LightGold.copy(alpha = 0.6f))
}
