package com.maestro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors

data class NavTab(val screen: Screen, val label: String, val icon: String)

val navTabs = listOf(
    NavTab(Screen.Dashboard, "Inicio", "♪"),
    NavTab(Screen.Students, "Estudiantes", "♬"),
    NavTab(Screen.Classes, "Clases", "📋"),
    NavTab(Screen.Finances, "Finanzas", "₿"),
    NavTab(Screen.Tasks, "Tareas", "✓"),
    NavTab(Screen.Events, "Agenda", "📅"),
    NavTab(Screen.Metronome, "Metrónomo", "𝅗𝅥"),
    NavTab(Screen.Guide, "Guía", "📖")
)

@Composable
fun AppScaffold(currentRoute: String, navController: NavHostController, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaestroColors.Cream)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaestroColors.Espresso)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaestroColors.Gold, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("♪", fontSize = 18.sp, color = MaestroColors.Espresso)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "MAESTRO",
                color = MaestroColors.Gold,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Nav bar
        Column(modifier = Modifier.fillMaxWidth().background(MaestroColors.White)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(navTabs) { tab ->
                    val selected = currentRoute == tab.screen.route
                    Column(
                        modifier = Modifier
                            .clickable { navController.navigate(tab.screen.route) { launchSingleTop = true } }
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(tab.icon, fontSize = 15.sp)
                            Text(
                                tab.label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaestroColors.Terra else MaestroColors.Muted
                            )
                        }
                        // Bottom indicator — full width of the tab
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    if (selected) MaestroColors.Terra
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                        )
                    }
                }
            }
            HorizontalDivider(color = MaestroColors.LightGold, thickness = 1.dp)
        }

        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}
