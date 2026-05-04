package com.maestro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
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

/** Five-line music staff drawn in gold — decorative sidebar divider. */
@Composable
fun Pentagram(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(14.dp)) {
        val color = Color(0xFFC9A84C).copy(alpha = 0.45f)
        listOf(0f, 3f, 6f, 9f, 12f).forEach { y ->
            drawLine(
                color = color,
                start = Offset(0f, y.dp.toPx()),
                end = Offset(size.width, y.dp.toPx()),
                strokeWidth = 0.6.dp.toPx()
            )
        }
    }
}

@Composable
fun MaestroSidebar(currentRoute: String, navController: NavHostController) {
    Column(
        modifier = Modifier
            .width(248.dp)
            .fillMaxHeight()
            .background(MaestroColors.Cream)
    ) {
        // Sidebar right border
        Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
            // Right border line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaestroColors.LightGold)
                    .align(Alignment.CenterEnd)
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Logo area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaestroColors.Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♪", fontSize = 16.sp, color = MaestroColors.Espresso)
                    }
                    Text(
                        "Maestro",
                        color = MaestroColors.Espresso,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                }

                // Pentagram divider
                Pentagram(modifier = Modifier.padding(horizontal = 20.dp))

                Spacer(Modifier.height(16.dp))

                // Nav items
                navTabs.forEach { tab ->
                    val selected = currentRoute == tab.screen.route ||
                        (tab.screen == Screen.Students && currentRoute.startsWith("student_detail"))
                    SidebarNavItem(
                        tab = tab,
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.screen.route) { launchSingleTop = true }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarNavItem(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp)) {
        // Left accent border for active item
        if (selected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .matchParentSize()
                    .background(MaestroColors.Terra, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .align(Alignment.CenterStart)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (selected) MaestroColors.White else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                tab.icon,
                fontSize = 16.sp,
                color = if (selected) MaestroColors.Terra else MaestroColors.Muted
            )
            Text(
                tab.label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaestroColors.Espresso else MaestroColors.Muted
            )
        }
    }
}

@Composable
fun MaestroCompactHeader(pageTitle: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaestroColors.Espresso)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mini logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaestroColors.Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("♪", fontSize = 12.sp, color = MaestroColors.Espresso)
                }
                Text(
                    "Maestro",
                    color = MaestroColors.Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }
            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(18.dp)
                    .background(MaestroColors.Gold.copy(alpha = 0.4f))
            )
            // Page title
            if (pageTitle.isNotEmpty()) {
                Text(
                    pageTitle,
                    color = MaestroColors.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        // Gold gradient hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaestroColors.Gold,
                            MaestroColors.LightGold,
                            MaestroColors.Gold.copy(alpha = 0.3f)
                        )
                    )
                )
        )
    }
}

@Composable
fun AppScaffold(
    currentRoute: String,
    navController: NavHostController,
    pageTitle: String = "",
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left sidebar
        MaestroSidebar(currentRoute, navController)

        // Right side: compact header + content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaestroColors.Cream)
        ) {
            MaestroCompactHeader(pageTitle)
            Box(modifier = Modifier.weight(1f)) { content() }
        }
    }
}
