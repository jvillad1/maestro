package com.maestro.app.ui.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.shared.repository.MaestroRepository
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.app.ui.components.LocalWindowWidthClass
import com.maestro.app.ui.components.WindowWidthClass
import com.maestro.app.ui.components.AttendanceChip
import com.maestro.app.ui.components.EmptyState
import com.maestro.app.ui.components.StatusChip
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Student
import com.maestro.shared.util.isValidIsoDate

private fun formatDate(date: String): String {
    val parts = date.split("-")
    if (parts.size != 3) return date
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val month = parts[1].toIntOrNull()?.minus(1)?.let { months.getOrNull(it) } ?: parts[1]
    return "${parts[2]} $month ${parts[0]}"
}

private fun studentInitial(students: List<Student>, studentId: String): String =
    students.find { it.id == studentId }?.name?.firstOrNull()?.uppercase() ?: "?"

private fun studentName(students: List<Student>, studentId: String): String =
    students.find { it.id == studentId }?.name ?: "Desconocido"

private fun studentColor(students: List<Student>, studentId: String): Color {
    val hex = students.find { it.id == studentId }?.color ?: "#C9A84C"
    val clean = hex.trimStart('#')
    if (clean.length < 6) return MaestroColors.Gold
    val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
    val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
    val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
    return Color(r / 255f, g / 255f, b / 255f)
}

@Composable
fun ClassesScreen(repository: MaestroRepository, navController: NavHostController) {
    val vm = viewModel { ClassesViewModel(repository) }
    val state by vm.state.collectAsStateWithLifecycle()

    var selectedStudentId by remember { mutableStateOf<String?>(null) }
    var dateInput by remember { mutableStateOf("") }
    var topicInput by remember { mutableStateOf("") }
    var paidInput by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }

    val focusDate = remember { FocusRequester() }
    val focusTopic = remember { FocusRequester() }

    AppScaffold(Screen.Classes.route, navController, pageTitle = "Clases", breadcrumb = "Agenda",
        subtitle = "${state.classes.size} clases registradas") {
        val compact = LocalWindowWidthClass.current == WindowWidthClass.Compact
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = if (compact) 16.dp else 28.dp, vertical = if (compact) 14.dp else 20.dp)) {
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            state.currentMonth,
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Muted
                        )
                        Text(
                            "${state.classes.size} clases registradas",
                            fontSize = 11.sp, color = MaestroColors.Muted
                        )
                    }
                    Button(
                        onClick = { vm.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("+ Registrar clase", color = MaestroColors.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaestroColors.Gold)
                    }
                } else if (state.classes.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.DateRange,
                        title = "Sin clases este mes",
                        subtitle = "Registra la primera clase del período\npara llevar el seguimiento.",
                        bgColor = MaestroColors.Terra,
                        actionLabel = "Registrar clase",
                        onAction = { vm.showAddDialog() }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header row — only meaningful with the desktop table layout
                            if (!compact) Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaestroColors.LightGold.copy(alpha = 0.45f))
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estudiante", modifier = Modifier.weight(2f), fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, color = MaestroColors.Muted, letterSpacing = 0.6.sp)
                                Text("Fecha", modifier = Modifier.weight(1f), fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, color = MaestroColors.Muted, letterSpacing = 0.6.sp)
                                Text("Estado", modifier = Modifier.weight(1f), fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, color = MaestroColors.Muted, letterSpacing = 0.6.sp)
                                Spacer(Modifier.width(100.dp))
                            }
                            if (!compact) HorizontalDivider(color = MaestroColors.LightGold)

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(state.classes) { cls ->
                                    ClassTableRow(
                                        classEntry = cls,
                                        students = state.students,
                                        compact = compact,
                                        onTogglePaid = { vm.togglePaid(cls) },
                                        onCycleAttendance = { vm.cycleAttendance(cls) },
                                        onOpenNotes = { vm.showNotesDialog(cls) }
                                    )
                                    HorizontalDivider(color = MaestroColors.LightGold.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // Add Class Dialog
            if (state.showAddDialog) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)).imePadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 480.dp).padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Registrar Clase", style = MaterialTheme.typography.titleMedium,
                                color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)

                            Text("Estudiante", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            state.students.forEach { student ->
                                val selected = selectedStudentId == student.id
                                OutlinedButton(
                                    onClick = { selectedStudentId = student.id },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) MaestroColors.LightGold else Color.Transparent,
                                        contentColor = MaestroColors.Espresso
                                    )
                                ) { Text(student.name) }
                            }

                            OutlinedTextField(
                                value = dateInput, onValueChange = { dateInput = it },
                                label = { Text("Fecha (YYYY-MM-DD)") },
                                placeholder = { Text("2024-01-15") },
                                isError = dateInput.isNotBlank() && !isValidIsoDate(dateInput),
                                supportingText = if (dateInput.isNotBlank() && !isValidIsoDate(dateInput)) {
                                    { Text("Fecha inválida — usa YYYY-MM-DD") }
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusDate)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) { focusTopic.requestFocus(); true } else false
                                    }
                            )
                            OutlinedTextField(
                                value = topicInput, onValueChange = { topicInput = it },
                                label = { Text("Tema de la clase") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusTopic)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) { focusDate.requestFocus(); true } else false
                                    }
                            )

                            OutlinedTextField(
                                value = notesInput, onValueChange = { notesInput = it },
                                label = { Text("Notas de la clase (opcional)") },
                                placeholder = { Text("Qué se vio, cómo respondió, tarea…") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Checkbox(checked = paidInput, onCheckedChange = { paidInput = it })
                                Text("Clase pagada", style = MaterialTheme.typography.bodyMedium, color = MaestroColors.Espresso)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                OutlinedButton(onClick = {
                                    vm.hideAddDialog()
                                    selectedStudentId = null; dateInput = ""; topicInput = ""; paidInput = false; notesInput = ""
                                }) { Text("Cancelar") }
                                Button(
                                    onClick = {
                                        selectedStudentId?.let { sid ->
                                            vm.createClass(sid, dateInput, topicInput, paidInput, notesInput)
                                            selectedStudentId = null; dateInput = ""; topicInput = ""; paidInput = false; notesInput = ""
                                        }
                                    },
                                    enabled = selectedStudentId != null && isValidIsoDate(dateInput) && topicInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra)
                                ) { Text("Guardar") }
                            }
                        }
                    }
                }
            }

            // Class log dialog — write what happened in this class
            state.editingNotesFor?.let { editing ->
                var draft by remember(editing.id) { mutableStateOf(editing.notes) }
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)).imePadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 480.dp).padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Notas de la clase", style = MaterialTheme.typography.titleMedium,
                                color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)
                            Text(
                                "${studentName(state.students, editing.studentId)} · ${formatDate(editing.date)}\n${editing.topic}",
                                style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted
                            )

                            OutlinedTextField(
                                value = draft, onValueChange = { draft = it },
                                label = { Text("Bitácora") },
                                placeholder = { Text("Qué se vio, cómo respondió, tarea para la próxima…") },
                                minLines = 5,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                                OutlinedButton(onClick = { vm.hideNotesDialog() }) { Text("Cancelar") }
                                Button(
                                    onClick = { vm.saveNotes(editing, draft) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra)
                                ) { Text("Guardar") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassTableRow(
    classEntry: ClassEntry,
    students: List<Student>,
    compact: Boolean,
    onTogglePaid: () -> Unit,
    onCycleAttendance: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val color = studentColor(students, classEntry.studentId)
    val hasNotes = classEntry.notes.isNotBlank()

    if (compact) {
        // Phone: two-line row; chips toggle state, the text block opens the log
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(studentInitial(students, classEntry.studentId), color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column(modifier = Modifier.weight(1f).clickable { onOpenNotes() }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(studentName(students, classEntry.studentId),
                        fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso, maxLines = 1)
                    if (hasNotes) Text("✎", fontSize = 11.sp, color = MaestroColors.Gold)
                }
                Text(
                    if (hasNotes) classEntry.notes else "${formatDate(classEntry.date)} · ${classEntry.topic}",
                    fontSize = 11.sp,
                    color = if (hasNotes) MaestroColors.Espresso.copy(alpha = 0.75f) else MaestroColors.Muted,
                    maxLines = 1
                )
            }
            AttendanceChip(attendance = classEntry.attendance, onClick = onCycleAttendance)
            StatusChip(paid = classEntry.paid, onClick = onTogglePaid)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Student column
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(studentInitial(students, classEntry.studentId), color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column {
                Text(studentName(students, classEntry.studentId),
                    fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso)
                Text(classEntry.topic, fontSize = 11.sp, color = MaestroColors.Muted)
                if (hasNotes) Text(classEntry.notes, fontSize = 11.sp,
                    color = MaestroColors.Espresso.copy(alpha = 0.7f), maxLines = 2)
            }
        }

        // Date column
        Text(formatDate(classEntry.date),
            modifier = Modifier.weight(1f),
            fontSize = 12.sp, color = MaestroColors.Espresso)

        // Status + action column
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AttendanceChip(attendance = classEntry.attendance, onClick = onCycleAttendance)
            StatusChip(paid = classEntry.paid)
        }

        // Toggle actions
        TextButton(onClick = onTogglePaid, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            Text(if (classEntry.paid) "Desmarcar" else "Marcar pagado",
                fontSize = 11.sp, color = MaestroColors.Terra)
        }
        TextButton(onClick = onOpenNotes, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            Text(if (hasNotes) "Ver notas" else "+ Notas", fontSize = 11.sp, color = MaestroColors.Gold)
        }
    }
}
