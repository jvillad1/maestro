package com.maestro.app.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors
import com.maestro.app.ui.components.AppScaffold

data class LevelGuide(
    val name: String,
    val color: Color,
    val duration: String,
    val objectives: List<String>,
    val repertoire: List<String>
)

val LEVEL_GUIDE_DATA = listOf(
    LevelGuide("Inicial", Color(0xFF7BC67E), "6-12 meses",
        listOf("Postura y posición al piano", "Lectura de notas básicas (Do-Sol)", "Ritmo con valores básicos", "Canciones simples con 5 dedos"),
        listOf("Pequeñas piezas de Bartók", "Canciones folclóricas adaptadas", "Método Suzuki Vol.1")),
    LevelGuide("Elemental", Color(0xFF64B5F6), "1-2 años",
        listOf("Escalas mayores (Do, Sol, Re)", "Lectura en clave de Fa", "Articulaciones básicas", "Piezas ABRSM 1-2"),
        listOf("Bach Anna Magdalena", "Clementi Sonatinas Op.36", "Bartók Mikrokosmos Vol.1-2")),
    LevelGuide("Intermedio", Color(0xFFFFB74D), "2-3 años",
        listOf("Escalas y arpegios completos", "Fraseo musical", "Pedal de resonancia", "Análisis formal básico"),
        listOf("Mozart Sonatas sencillas", "Beethoven Sonatinas", "Chopin Preludios fáciles")),
    LevelGuide("Avanzado", Color(0xFFF06292), "3+ años",
        listOf("Técnica avanzada", "Repertorio de concierto", "Interpretación estilística", "Preparación para audiciones"),
        listOf("Bach Invenciones / Partitas", "Beethoven Sonatas Op.13, 27", "Chopin Baladas, Nocturnos"))
)

@Composable
fun GuideScreen(navController: NavHostController) {
    AppScaffold(Screen.Guide.route, navController) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Guía de Niveles", style = MaterialTheme.typography.headlineMedium, color = MaestroColors.Espresso)
                Text("Referencia pedagógica por nivel", style = MaterialTheme.typography.bodyMedium, color = MaestroColors.Muted)
                Spacer(Modifier.height(8.dp))
            }
            items(LEVEL_GUIDE_DATA) { level ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(level.color, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(level.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                                Text("Duración: ${level.duration}", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Objetivos", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            level.objectives.forEach { Text("→ $it", color = MaestroColors.Espresso, style = MaterialTheme.typography.bodySmall) }
                            Spacer(Modifier.height(4.dp))
                            Text("Repertorio sugerido", style = MaterialTheme.typography.labelMedium, color = MaestroColors.Muted)
                            level.repertoire.forEach { Text("♩ $it", color = MaestroColors.Espresso, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}
