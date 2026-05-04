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
        Row(
            modifier = Modifier.fillMaxWidth().background(MaestroColors.Espresso).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MAESTRO", color = MaestroColors.Gold, fontSize = 18.sp, letterSpacing = 2.sp)
        }
        LazyRow(modifier = Modifier.fillMaxWidth().background(MaestroColors.White)) {
            items(navTabs) { tab ->
                val selected = currentRoute == tab.screen.route
                Column(
                    modifier = Modifier
                        .clickable { navController.navigate(tab.screen.route) { launchSingleTop = true } }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tab.icon, fontSize = 16.sp)
                    Text(tab.label, fontSize = 12.sp,
                        color = if (selected) MaestroColors.Terra else MaestroColors.Muted)
                    if (selected) Box(modifier = Modifier.height(3.dp).width(40.dp).background(MaestroColors.Terra))
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) { content() }
    }
}
