package com.maestro.app.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.maestro.shared.model.Priority
import com.maestro.shared.model.Task

private fun priorityLabel(priority: Priority): String = when (priority) {
    Priority.ALTA -> "Alta"
    Priority.MEDIA -> "Media"
    Priority.BAJA -> "Baja"
}

private fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.ALTA -> Color(0xFFC0392B)
    Priority.MEDIA -> Color(0xFFE67E22)
    Priority.BAJA -> MaestroColors.Forest
}

private fun priorityBgColor(priority: Priority): Color = when (priority) {
    Priority.ALTA -> Color(0xFFFDECEA)
    Priority.MEDIA -> Color(0xFFFEF9E7)
    Priority.BAJA -> MaestroColors.SoftGreen
}

@Composable
fun TasksScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { TasksViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIA) }

    // Single field — Tab stays on the same field (no-op)
    val focusText = remember { FocusRequester() }

    AppScaffold(Screen.Tasks.route, navController) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lista de Tareas",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaestroColors.Espresso,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { vm.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Nueva Tarea", color = MaestroColors.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaestroColors.Gold)
                    }
                } else {
                    val grouped = state.tasks.groupBy { it.priority }
                    val priorityOrder = listOf(Priority.ALTA, Priority.MEDIA, Priority.BAJA)

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        priorityOrder.forEach { priority ->
                            val tasks = grouped[priority] ?: emptyList()
                            if (tasks.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.background(
                                                priorityBgColor(priority),
                                                RoundedCornerShape(4.dp)
                                            ).padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                "Prioridad ${priorityLabel(priority)}",
                                                fontSize = 11.sp,
                                                color = priorityColor(priority),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text(
                                            "${tasks.size} tareas",
                                            fontSize = 11.sp,
                                            color = MaestroColors.Muted
                                        )
                                    }
                                }
                                items(tasks) { task ->
                                    TaskItem(
                                        task = task,
                                        onToggle = { vm.toggleDone(task) },
                                        onDelete = { vm.deleteTask(task) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add Task Dialog
            if (state.showAddDialog) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.widthIn(max = 420.dp).padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Nueva Tarea",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaestroColors.Espresso,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = textInput, onValueChange = { textInput = it },
                                label = { Text("Descripción de la tarea") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusText)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusText.requestFocus(); true
                                        } else false
                                    },
                                minLines = 2
                            )

                            // Priority selector
                            Text("Prioridad", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Priority.entries.forEach { priority ->
                                    val sel = priority == selectedPriority
                                    Button(
                                        onClick = { selectedPriority = priority },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (sel) priorityColor(priority) else priorityBgColor(priority),
                                            contentColor = if (sel) MaestroColors.White else priorityColor(priority)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(priorityLabel(priority), fontSize = 12.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                OutlinedButton(onClick = {
                                    vm.hideAddDialog()
                                    textInput = ""
                                    selectedPriority = Priority.MEDIA
                                }) {
                                    Text("Cancelar")
                                }
                                Button(
                                    onClick = {
                                        if (textInput.isNotBlank()) {
                                            vm.createTask(textInput, selectedPriority)
                                            textInput = ""
                                            selectedPriority = Priority.MEDIA
                                        }
                                    },
                                    enabled = textInput.isNotBlank(),
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
private fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    val isDone = task.done
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDone) MaestroColors.White.copy(alpha = 0.6f) else MaestroColors.White),
        elevation = CardDefaults.cardElevation(if (isDone) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaestroColors.Forest,
                    uncheckedColor = MaestroColors.Muted
                )
            )
            Text(
                task.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDone) MaestroColors.Muted else MaestroColors.Espresso,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            Box(
                modifier = Modifier.background(priorityBgColor(task.priority), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(priorityLabel(task.priority), fontSize = 9.sp, color = priorityColor(task.priority))
            }
            TextButton(
                onClick = onDelete,
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("✕", color = MaestroColors.Muted, fontSize = 12.sp)
            }
        }
    }
}
