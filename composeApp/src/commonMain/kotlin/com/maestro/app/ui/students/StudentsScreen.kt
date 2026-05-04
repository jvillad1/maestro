package com.maestro.app.ui.students

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.shared.model.Level
import com.maestro.shared.model.Student

private fun parseHexColor(hex: String): Color {
    val clean = hex.trimStart('#')
    if (clean.length < 6) return MaestroColors.Gold
    val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
    val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
    val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
    return Color(r / 255f, g / 255f, b / 255f)
}

private fun levelLabel(level: Level): String = when (level) {
    Level.INICIAL -> "Inicial"
    Level.ELEMENTAL -> "Elemental"
    Level.INTERMEDIO -> "Intermedio"
    Level.AVANZADO -> "Avanzado"
}

private val levelObjectives = mapOf(
    Level.INICIAL to listOf(
        "Postura correcta",
        "Lectura de notas en clave de sol",
        "Escalas de Do y Sol mayor",
        "Piezas simples de un dedo"
    ),
    Level.ELEMENTAL to listOf(
        "Lectura en clave de fa",
        "Acordes básicos (I, IV, V)",
        "Escalas con ambas manos",
        "Repertorio barroco sencillo"
    ),
    Level.INTERMEDIO to listOf(
        "Polifonía a 2 voces",
        "Escalas menores",
        "Dinámica y matices",
        "Obras de Bach, Clementi"
    ),
    Level.AVANZADO to listOf(
        "Repertorio avanzado",
        "Análisis armónico",
        "Pedal y expresividad",
        "Obras de Chopin, Mozart, Beethoven"
    )
)

private val presetColors = listOf("#C9A84C", "#B85C38", "#2D5016", "#3D6E8F", "#8B4A8B", "#4A8B6F")

@Composable
fun StudentsScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { StudentsViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()

    // Add student dialog state
    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var feeInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(Level.INICIAL) }
    var colorIndex by remember { mutableStateOf(0) }

    // Focus requesters: name -> age -> phone -> email -> fee -> notes -> name (circular)
    val focusName = remember { FocusRequester() }
    val focusAge = remember { FocusRequester() }
    val focusPhone = remember { FocusRequester() }
    val focusEmail = remember { FocusRequester() }
    val focusFee = remember { FocusRequester() }
    val focusNotes = remember { FocusRequester() }

    AppScaffold(Screen.Students.route, navController) {
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
                            "Mis Estudiantes",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaestroColors.Espresso,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${state.students.size} estudiantes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaestroColors.Muted
                        )
                    }
                    Button(
                        onClick = { vm.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Nuevo Estudiante", color = MaestroColors.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaestroColors.Gold)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 260.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.students) { student ->
                            StudentCard(student = student, onClick = {
                                navController.navigate(Screen.StudentDetail.route(student.id))
                            })
                        }
                    }
                }
            }

            // Add Student Dialog
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
                                "Nuevo Estudiante",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaestroColors.Espresso,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = nameInput, onValueChange = { nameInput = it },
                                label = { Text("Nombre") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusName)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusAge.requestFocus(); true
                                        } else false
                                    }
                            )
                            OutlinedTextField(
                                value = ageInput, onValueChange = { ageInput = it },
                                label = { Text("Edad") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusAge)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusPhone.requestFocus(); true
                                        } else false
                                    }
                            )
                            OutlinedTextField(
                                value = phoneInput, onValueChange = { phoneInput = it },
                                label = { Text("Teléfono") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusPhone)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusEmail.requestFocus(); true
                                        } else false
                                    }
                            )
                            OutlinedTextField(
                                value = emailInput, onValueChange = { emailInput = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusEmail)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusFee.requestFocus(); true
                                        } else false
                                    }
                            )
                            OutlinedTextField(
                                value = feeInput, onValueChange = { feeInput = it },
                                label = { Text("Cuota mensual (COP)") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusFee)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusNotes.requestFocus(); true
                                        } else false
                                    }
                            )
                            OutlinedTextField(
                                value = notesInput, onValueChange = { notesInput = it },
                                label = { Text("Notas pedagógicas") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusNotes)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusName.requestFocus(); true
                                        } else false
                                    },
                                minLines = 2
                            )

                            // Level selector
                            Text("Nivel", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Level.entries.forEach { lvl ->
                                    val sel = lvl == selectedLevel
                                    OutlinedButton(
                                        onClick = { selectedLevel = lvl },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (sel) MaestroColors.Espresso else Color.Transparent,
                                            contentColor = if (sel) MaestroColors.White else MaestroColors.Espresso
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(levelLabel(lvl), fontSize = 11.sp)
                                    }
                                }
                            }

                            // Color selector
                            Text("Color", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                presetColors.forEachIndexed { idx, hex ->
                                    val sel = idx == colorIndex
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(parseHexColor(hex))
                                            .then(if (sel) Modifier.border(2.dp, MaestroColors.Espresso, CircleShape) else Modifier)
                                            .clickable { colorIndex = idx }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                OutlinedButton(onClick = { vm.hideAddDialog() }) {
                                    Text("Cancelar")
                                }
                                Button(
                                    onClick = {
                                        vm.createStudent(
                                            name = nameInput,
                                            age = ageInput.toIntOrNull() ?: 0,
                                            level = selectedLevel,
                                            phone = phoneInput,
                                            email = emailInput,
                                            monthlyFee = feeInput.toLongOrNull() ?: 0L,
                                            notes = notesInput,
                                            color = presetColors[colorIndex]
                                        )
                                        nameInput = ""; ageInput = ""; phoneInput = ""
                                        emailInput = ""; feeInput = ""; notesInput = ""
                                        selectedLevel = Level.INICIAL; colorIndex = 0
                                    },
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
private fun StudentCard(student: Student, onClick: () -> Unit) {
    val studentColor = parseHexColor(student.color)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left color border
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(studentColor))
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Avatar circle
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(studentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            student.name.firstOrNull()?.uppercase() ?: "?",
                            color = studentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, style = MaterialTheme.typography.titleSmall, color = MaestroColors.Espresso, fontWeight = FontWeight.SemiBold)
                        Text("${student.age} años · ${student.phone}", style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
                    }
                    // Level badge
                    Box(
                        modifier = Modifier.background(studentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(levelLabel(student.level), fontSize = 10.sp, color = studentColor, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.background(MaestroColors.LightGold, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("$${student.monthlyFee.toString().reversed().chunked(3).joinToString(".").reversed()}/mes",
                            fontSize = 11.sp, color = MaestroColors.Espresso, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDetailScreen(studentId: Long, apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { StudentDetailViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(studentId) { vm.load(studentId) }

    AppScaffold(Screen.StudentDetail.route, navController) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Back button
            TextButton(onClick = { navController.popBackStack() }) {
                Text("← Volver a Estudiantes", color = MaestroColors.Terra)
            }

            Spacer(Modifier.height(8.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaestroColors.Gold)
                }
                return@AppScaffold
            }

            val student = state.student ?: return@AppScaffold

            val studentColor = parseHexColor(student.color)

            // Student info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(studentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                student.name.firstOrNull()?.uppercase() ?: "?",
                                color = studentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                        Column {
                            Text(student.name, style = MaterialTheme.typography.titleMedium, color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier.background(studentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(levelLabel(student.level), fontSize = 11.sp, color = studentColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    HorizontalDivider(color = MaestroColors.LightGold)
                    InfoRow("Edad", "${student.age} años")
                    InfoRow("Teléfono", student.phone)
                    InfoRow("Email", student.email)
                    InfoRow("Cuota mensual", "$${student.monthlyFee.toString().reversed().chunked(3).joinToString(".").reversed()}")
                    InfoRow("Desde", student.joinDate)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pedagogical notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notas Pedagógicas", style = MaterialTheme.typography.titleSmall, color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)
                    if (student.notes.isNotBlank()) {
                        Text(student.notes, style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Objetivos ${levelLabel(student.level)}", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Espresso, fontWeight = FontWeight.SemiBold)
                    levelObjectives[student.level]?.forEach { obj ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•", color = MaestroColors.Gold)
                            Text(obj, style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Class history
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Historial de Clases", style = MaterialTheme.typography.titleSmall, color = MaestroColors.Espresso, fontWeight = FontWeight.Bold)
                    if (state.classes.isEmpty()) {
                        Text("Sin clases registradas", style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
                    } else {
                        state.classes.forEach { cls ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.background(MaestroColors.LightGold, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(cls.date, fontSize = 10.sp, color = MaestroColors.Espresso)
                                    }
                                    Text(cls.topic, style = MaterialTheme.typography.bodySmall, color = MaestroColors.Espresso)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.background(
                                            if (cls.paid) MaestroColors.SoftGreen else Color(0xFFFDECEA),
                                            RoundedCornerShape(4.dp)
                                        ).padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (cls.paid) "Pagado" else "Pendiente",
                                            fontSize = 10.sp,
                                            color = if (cls.paid) MaestroColors.Forest else Color(0xFFC0392B)
                                        )
                                    }
                                    TextButton(
                                        onClick = { vm.togglePaid(cls) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (cls.paid) "Desmarcar" else "Marcar pagado",
                                            fontSize = 10.sp, color = MaestroColors.Terra
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaestroColors.LightGold.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaestroColors.Espresso, fontWeight = FontWeight.Medium)
    }
}
