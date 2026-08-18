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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
import com.maestro.app.ui.components.LocalWindowWidthClass
import com.maestro.app.ui.components.WindowWidthClass
import com.maestro.app.ui.dashboard.getCurrentMonth

private fun formatCOP(amount: Long): String =
    "$${amount.toString().reversed().chunked(3).joinToString(".").reversed()}"

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

/** "2026-08" → "Agosto 2026" */
private fun monthLabel(month: String): String {
    val parts = month.split("-")
    if (parts.size != 2) return month
    val name = parts[1].toIntOrNull()?.minus(1)?.let { MONTH_NAMES.getOrNull(it) } ?: return month
    return "$name ${parts[0]}"
}

private fun parseHexColor(hex: String): Color {
    val clean = hex.trimStart('#')
    if (clean.length < 6) return MaestroColors.Gold
    val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
    val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
    val b = clean.substring(4, 6).toIntOrNull(16) ?: 0
    return Color(r / 255f, g / 255f, b / 255f)
}

/** Resumen en texto plano, listo para pegar en WhatsApp o un correo. */
private fun reportText(state: FinancesState): String = buildString {
    appendLine("MAESTRO · Reporte de ${monthLabel(state.selectedMonth)}")
    appendLine()
    appendLine("Esperado:  ${formatCOP(state.expectedTotal)}")
    appendLine("Cobrado:   ${formatCOP(state.collected)}")
    appendLine("Pendiente: ${formatCOP(state.pending)}")
    appendLine()
    appendLine("Por estudiante:")
    state.rows.forEach { r ->
        val estado = when (r.status) {
            FinanceStatus.PAID -> "pagado"
            FinanceStatus.PENDING -> "pendiente"
            FinanceStatus.NO_CLASSES -> "sin clases"
        }
        append("· ${r.student.name}: ${r.classCount} clases")
        if (r.attended > 0 || r.missed > 0) append(" (${r.attended} asistió, ${r.missed} faltó)")
        appendLine(" — ${formatCOP(r.student.monthlyFee)} $estado")
    }
}

@Composable
fun FinancesScreen(repository: MaestroRepository, navController: NavHostController) {
    val vm = viewModel { FinancesViewModel(repository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AppScaffold(Screen.Finances.route, navController, pageTitle = "Finanzas", breadcrumb = "Gestión") {
        val compact = LocalWindowWidthClass.current == WindowWidthClass.Compact
        Column(modifier = Modifier.fillMaxSize().padding(if (compact) 16.dp else 20.dp)) {
            Text(
                "Reporte Mensual",
                style = MaterialTheme.typography.titleLarge,
                color = MaestroColors.Espresso,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            // Month navigation — the report is the same view for any past month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { copied = false; vm.previousMonth() }) {
                    Text("‹", fontSize = 24.sp, color = MaestroColors.Terra, fontWeight = FontWeight.Bold)
                }
                Text(
                    monthLabel(state.selectedMonth),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaestroColors.Espresso,
                    fontWeight = FontWeight.SemiBold
                )
                val canGoForward = state.selectedMonth < getCurrentMonth()
                IconButton(onClick = { copied = false; vm.nextMonth() }, enabled = canGoForward) {
                    Text(
                        "›", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = if (canGoForward) MaestroColors.Terra else MaestroColors.Muted.copy(alpha = 0.35f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

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

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Detalle por Estudiante",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaestroColors.Espresso,
                    fontWeight = FontWeight.Bold
                )
                if (state.students.isNotEmpty()) TextButton(onClick = {
                    clipboard.setText(AnnotatedString(reportText(state)))
                    copied = true
                }) {
                    Text(
                        if (copied) "✓ Copiado" else "Copiar resumen",
                        fontSize = 12.sp,
                        color = if (copied) MaestroColors.Forest else MaestroColors.Terra,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.students.isEmpty()) {
                Text(
                    "Sin estudiantes para reportar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaestroColors.Muted
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows) { row ->
                        FinanceStudentRow(row = row)
                    }
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
        val compact = LocalWindowWidthClass.current == WindowWidthClass.Compact
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = textColor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                fontSize = if (compact) 12.5.sp else 14.sp, maxLines = 1)
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
                    // Attendance only reads as a fact once something was marked
                    if (row.attended > 0 || row.missed > 0) {
                        Text(
                            "${row.attended} asistió · ${row.missed} faltó",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = if (row.missed > 0) Color(0xFF9B3B30) else MaestroColors.Forest
                        )
                    }
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
