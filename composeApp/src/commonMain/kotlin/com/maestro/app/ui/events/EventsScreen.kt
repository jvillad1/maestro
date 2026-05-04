package com.maestro.app.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.app.ui.dashboard.getCurrentDate
import com.maestro.shared.model.Event
import com.maestro.shared.model.EventType

private fun eventTypeLabel(type: EventType): String = when (type) {
    EventType.RECITAL -> "Recital"
    EventType.MASTERCLASS -> "Masterclass"
    EventType.EVALUACION -> "Evaluación"
    EventType.OTRO -> "Otro"
}

private fun eventTypeColor(type: EventType): Color = when (type) {
    EventType.RECITAL -> MaestroColors.Terra
    EventType.MASTERCLASS -> MaestroColors.Forest
    EventType.EVALUACION -> Color(0xFF7B68EE)
    EventType.OTRO -> MaestroColors.Muted
}

private fun extractDay(date: String): String {
    val parts = date.split("-")
    return if (parts.size == 3) parts[2] else date
}

private fun extractMonthName(date: String): String {
    val parts = date.split("-")
    if (parts.size != 3) return ""
    val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    return parts[1].toIntOrNull()?.minus(1)?.let { months.getOrNull(it) } ?: parts[1]
}

@Composable
fun EventsScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { EventsViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()
    val today = remember { getCurrentDate() }

    var titleInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.RECITAL) }

    // Focus requesters: title -> date -> description -> title (circular)
    val focusTitle = remember { FocusRequester() }
    val focusDate = remember { FocusRequester() }
    val focusDescription = remember { FocusRequester() }

    AppScaffold(Screen.Events.route, navController) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Agenda & Eventos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaestroColors.Espresso,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { vm.showAddDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Nuevo Evento", color = MaestroColors.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaestroColors.Gold)
                    }
                } else if (state.events.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin eventos próximos", color = MaestroColors.Muted)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.events) { event ->
                            EventCard(event = event, today = today)
                        }
                    }
                }
            }

            // Add Event Dialog
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
                                "Nuevo Evento",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaestroColors.Espresso,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = titleInput, onValueChange = { titleInput = it },
                                label = { Text("Título del evento") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusTitle)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusDate.requestFocus(); true
                                        } else false
                                    }
                            )
                            OutlinedTextField(
                                value = dateInput, onValueChange = { dateInput = it },
                                label = { Text("Fecha (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusDate)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusDescription.requestFocus(); true
                                        } else false
                                    },
                                placeholder = { Text("2024-06-15") }
                            )
                            OutlinedTextField(
                                value = descriptionInput, onValueChange = { descriptionInput = it },
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth()
                                    .focusRequester(focusDescription)
                                    .onPreviewKeyEvent { e ->
                                        if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                            focusTitle.requestFocus(); true
                                        } else false
                                    },
                                minLines = 2
                            )

                            // Type selector
                            Text("Tipo de evento", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                EventType.entries.forEach { type ->
                                    val sel = type == selectedType
                                    val typeColor = eventTypeColor(type)
                                    Button(
                                        onClick = { selectedType = type },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (sel) typeColor else typeColor.copy(alpha = 0.12f),
                                            contentColor = if (sel) MaestroColors.White else typeColor
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(eventTypeLabel(type), fontSize = 10.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) {
                                OutlinedButton(onClick = {
                                    vm.hideAddDialog()
                                    titleInput = ""; dateInput = ""; descriptionInput = ""
                                    selectedType = EventType.RECITAL
                                }) {
                                    Text("Cancelar")
                                }
                                Button(
                                    onClick = {
                                        if (titleInput.isNotBlank() && dateInput.isNotBlank()) {
                                            vm.createEvent(titleInput, dateInput, selectedType, descriptionInput)
                                            titleInput = ""; dateInput = ""; descriptionInput = ""
                                            selectedType = EventType.RECITAL
                                        }
                                    },
                                    enabled = titleInput.isNotBlank() && dateInput.isNotBlank(),
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
private fun EventCard(event: Event, today: String) {
    val typeColor = eventTypeColor(event.type)
    val isPast = event.date < today

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPast) MaestroColors.White.copy(alpha = 0.6f) else MaestroColors.White),
        elevation = CardDefaults.cardElevation(if (isPast) 0.dp else 2.dp)
    ) {
        Column {
            // Colored header bar
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    if (isPast) typeColor.copy(alpha = 0.4f) else typeColor,
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ).padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                        Text(
                            extractDay(event.date),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaestroColors.White
                        )
                        Text(
                            extractMonthName(event.date),
                            fontSize = 13.sp,
                            color = MaestroColors.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.background(MaestroColors.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(eventTypeLabel(event.type), fontSize = 9.sp, color = MaestroColors.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPast) MaestroColors.Muted else MaestroColors.Espresso
                )
                if (event.description.isNotBlank()) {
                    Text(
                        event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaestroColors.Muted,
                        maxLines = 2
                    )
                }
                if (isPast) {
                    Text("Pasado", fontSize = 10.sp, color = MaestroColors.Muted)
                }
            }
        }
    }
}
