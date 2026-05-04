package com.maestro.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maestro.app.network.ApiClient
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold

@Composable
fun DashboardScreen(apiClient: ApiClient, navController: NavHostController) {
    val vm = viewModel { DashboardViewModel(apiClient) }
    val state by vm.state.collectAsStateWithLifecycle()
    val phrase = PHRASES[state.currentPhraseIndex]

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(8000)
            vm.nextPhrase()
        }
    }

    AppScaffold(Screen.Dashboard.route, navController) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaestroColors.Gold)
            }
            return@AppScaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaestroColors.Espresso)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Frase del día", color = MaestroColors.Gold, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("\"${phrase.text}\"", color = MaestroColors.White, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("— ${phrase.author}", color = MaestroColors.Gold, style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Estudiantes", state.students.size.toString(), MaestroColors.Forest, MaestroColors.SoftGreen, Modifier.weight(1f))
                StatCard("Cobrado", formatCOP(state.totalIncome), MaestroColors.Forest, MaestroColors.SoftGreen, Modifier.weight(1f))
                StatCard("Por cobrar", formatCOP(state.pendingIncome), Color(0xFFC0392B), Color(0xFFFDECEA), Modifier.weight(1f))
            }

            if (state.pendingTasks.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaestroColors.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tareas Pendientes", style = MaterialTheme.typography.titleSmall, color = MaestroColors.Espresso)
                            TextButton(onClick = { navController.navigate(Screen.Tasks.route) }) {
                                Text("Ver todas →", color = MaestroColors.Terra)
                            }
                        }
                        state.pendingTasks.forEach { task ->
                            Text("• ${task.text}", color = MaestroColors.Espresso,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, textColor: Color, bgColor: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, color = textColor, style = MaterialTheme.typography.titleMedium)
            Text(label, color = MaestroColors.Muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatCOP(amount: Long): String =
    "$${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"
