package com.maestro.app.ui.classes

import androidx.compose.foundation.background
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
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.shared.model.ClassEntry
import com.maestro.shared.model.Student

private fun formatDate(date: String): String {
    val parts = date.split("-")
    if (parts.size != 3) return date
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val month = parts[1].toIntOrNull()?.minus(1)?.let { months.getOrNull(it) } ?: parts[1]
    return "${parts[2]} $month ${parts[0]}"
}

private fun studentInitial(students: List<Student>, studentId: Long): String =
    students.find { it.id == studentId }?.name?.firstOrNull()?.uppercase() ?: "?"

private fun studentName(students: List<Student>, studentId: Long): String =
    students.find { it.id == studentId }?.name ?: "Desconocido"

private fun studentColor(students: List<Student>, studentId: Long): Color {
    val hex = students.find { it.id == studentId }?.color ?: "#C9A84C"
    val clean = hex.trimStart('#')
    if (clean.length < 6) return MaestroColors.Gold
    val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
    val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
    val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
    return Color(r / 255f, g / 255f, b / 255f)
}

@Composable
fun ClassesScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { ClassesViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()

    var selectedStudentId by remember { mutableStateOf<Long?>(null) }
    var dateInput by remember { mutableStateOf("") }
    var topicInput by remember { mutableStateOf("") }
    var paidInput by remember { mutableStateOf(false) }

    // Focus requesters: date -> topic -> date (circular)
    val focusDate = remember { FocusRequester() }
    val focusTopic = remember { FocusRequester() }

    AppScaffold(Screen.Classes.route, navController, pageTitle = "Clases") {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Registro de Clases",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaestroColors.Espresso,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.currentMonth,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaestroColors.Muted
                        )
                    }
                    Button(
                        onClick = { vm.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Registrar Clase", color = MaestroColors.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaestroColors.Gold)
                    }
                } else if (state.classes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin clases este mes", color = MaestroColors.Muted)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.classes) { cls ->
                            ClassItem(
                                classEntry = cls,
                                students = state.students,
                                onTogglePaid = { vm.togglePaid(cls) }
                            )
                        }
                    }
                }
            }

            // Add Class Dialog
            if (state.showAddDialog) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
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
                            Text(
                                "Registrar Clase",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaestroColors.Espresso,
                                fontWeight = FontWeight.Bold
                            )

                            // Student selector
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
                                ) {
                                    Text(student.name)
                                }
                            }

                            OutlinedTextField(
                                value = dateInput, onValueChange = { dateInput = it },
                                label = { Text("Fecha (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusDate)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusTopic.requestFocus(); true
                                        } else false
                                    },
                                placeholder = { Text("2024-01-15") }
                            )
                            OutlinedTextField(
                                value = topicInput, onValueChange = { topicInput = it },
                                label = { Text("Tema de la clase") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusTopic)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusDate.requestFocus(); true
                                        } else false
                                    }
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Checkbox(checked = paidInput, onCheckedChange = { paidInput = it })
                                Text("Clase pagada", style = MaterialTheme.typography.bodyMedium, color = MaestroColors.Espresso)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                OutlinedButton(onClick = {
                                    vm.hideAddDialog()
                                    selectedStudentId = null
                                    dateInput = ""; topicInput = ""; paidInput = false
                                }) {
                                    Text("Cancelar")
                                }
                                Button(
                                    onClick = {
                                        selectedStudentId?.let { sid ->
                                            vm.createClass(sid, dateInput, topicInput, paidInput)
                                            selectedStudentId = null
                                            dateInput = ""; topicInput = ""; paidInput = false
                                        }
                                    },
                                    enabled = selectedStudentId != null && dateInput.isNotBlank() && topicInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra)
                                ) {
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassItem(classEntry: ClassEntry, students: List<Student>, onTogglePaid: () -> Unit) {
    val color = studentColor(students, classEntry.studentId)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(studentInitial(students, classEntry.studentId), color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text(studentName(students, classEntry.studentId), style = MaterialTheme.typography.bodyMedium, color = MaestroColors.Espresso, fontWeight = FontWeight.SemiBold)
                    Text(classEntry.topic, style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
                    Text(formatDate(classEntry.date), style = MaterialTheme.typography.labelSmall, color = MaestroColors.Muted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.background(
                        if (classEntry.paid) MaestroColors.SoftGreen else Color(0xFFFDECEA),
                        RoundedCornerShape(4.dp)
                    ).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (classEntry.paid) "Pagado" else "Pendiente",
                        fontSize = 10.sp,
                        color = if (classEntry.paid) MaestroColors.Forest else Color(0xFFC0392B)
                    )
                }
                TextButton(onClick = onTogglePaid, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(if (classEntry.paid) "Desmarcar" else "Marcar pagado", fontSize = 10.sp, color = MaestroColors.Terra)
                }
            }
        }
    }
}
