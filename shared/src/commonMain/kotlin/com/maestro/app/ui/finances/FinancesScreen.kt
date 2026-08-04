package com.maestro.app.ui.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.shared.repository.MaestroRepository
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold
import com.maestro.shared.model.Student

private fun formatCOP(amount: Long): String =
    "$${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"

private fun parseHexColor(hex: String): Color {
    val clean = hex.trimStart('#')
    if (clean.length < 6) return MaestroColors.Gold
    val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
    val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
    val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
    return Color(r / 255f, g / 255f, b / 255f)
}

@Composable
fun FinancesScreen(repository: MaestroRepository, navController: NavHostController) {
    val vm = viewModel { FinancesViewModel(repository) }
    val state by vm.state.collectAsStateWithLifecycle()

    AppScaffold(Screen.Finances.route, navController, pageTitle = "Finanzas", breadcrumb = "Gestión") {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            // Header
            Text(
                "Control Financiero",
                style = MaterialTheme.typography.titleLarge,
                color = MaestroColors.Espresso,
                fontWeight = FontWeight.Bold
            )
            Text(
                state.currentMonth,
                style = MaterialTheme.typography.bodySmall,
                color = MaestroColors.Muted
            )

            Spacer(Modifier.height(16.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaestroColors.Gold)
                }
                return@AppScaffold
            }

            // Stat cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinanceStatCard(
                    label = "Esperado",
                    value = formatCOP(state.expectedTotal),
                    bgColor = MaestroColors.LightGold,
                    textColor = MaestroColors.Espresso,
                    modifier = Modifier.weight(1f)
                )
                FinanceStatCard(
                    label = "Cobrado",
                    value = formatCOP(state.collected),
                    bgColor = MaestroColors.SoftGreen,
                    textColor = MaestroColors.Forest,
                    modifier = Modifier.weight(1f)
                )
                FinanceStatCard(
                    label = "Pendiente",
                    value = formatCOP(state.pending),
                    bgColor = Color(0xFFFDECEA),
                    textColor = Color(0xFFC0392B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Detalle por Estudiante",
                style = MaterialTheme.typography.titleSmall,
                color = MaestroColors.Espresso,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.rows) { row ->
                    FinanceStudentRow(row = row)
                }
            }
        }
    }
}

@Composable
private fun FinanceStatCard(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = textColor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(label, color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun FinanceStudentRow(row: StudentFinanceRow) {
    val studentColor = parseHexColor(row.student.color)
    val (statusText, statusBg, statusFg) = when (row.status) {
        FinanceStatus.PAID -> Triple("Pagado", MaestroColors.SoftGreen, MaestroColors.Forest)
        FinanceStatus.PENDING -> Triple("Pendiente", Color(0xFFFDECEA), Color(0xFFC0392B))
        FinanceStatus.NO_CLASSES -> Triple("Sin clases", MaestroColors.LightGold, MaestroColors.Muted)
    }

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
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(studentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(row.student.name.firstOrNull()?.uppercase() ?: "?", color = studentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text(row.student.name, style = MaterialTheme.typography.bodyMedium, color = MaestroColors.Espresso, fontWeight = FontWeight.SemiBold)
                    Text("${row.classCount} clases · ${formatCOP(row.student.monthlyFee)}/mes", style = MaterialTheme.typography.bodySmall, color = MaestroColors.Muted)
                }
            }
            Box(
                modifier = Modifier.background(statusBg, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(statusText, fontSize = 11.sp, color = statusFg, fontWeight = FontWeight.Medium)
            }
        }
    }
}
