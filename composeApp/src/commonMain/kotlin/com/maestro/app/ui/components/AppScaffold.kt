package com.maestro.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors
import com.maestro.app.theme.frauncesFamily

data class NavTab(val screen: Screen, val label: String, val icon: ImageVector)

val navTabs = listOf(
    NavTab(Screen.Dashboard,  "Inicio",      Icons.Default.Home),
    NavTab(Screen.Students,   "Estudiantes", Icons.Default.Person),
    NavTab(Screen.Classes,    "Clases",      Icons.Default.DateRange),
    NavTab(Screen.Finances,   "Finanzas",    Icons.Default.Payments),
    NavTab(Screen.Tasks,      "Tareas",      Icons.Default.CheckCircle),
    NavTab(Screen.Events,     "Agenda",      Icons.Default.Event),
    NavTab(Screen.Metronome,  "Metrónomo",   Icons.Default.MusicNote),
    NavTab(Screen.Guide,      "Guía",        Icons.Default.MenuBook),
)

// Five-line music staff — decorative gold divider
@Composable
fun Pentagram(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(14.dp)) {
        val color = Color(0xFFC9A84C).copy(alpha = 0.45f)
        listOf(0f, 3f, 6f, 9f, 12f).forEach { y ->
            drawLine(color, Offset(0f, y.dp.toPx()), Offset(size.width, y.dp.toPx()), 0.6.dp.toPx())
        }
    }
}

// Sidebar avatar with initials
@Composable
private fun InitialsAvatar(name: String, size: Int = 36, bgColor: Color = MaestroColors.Terra) {
    val initials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    Box(
        modifier = Modifier.size(size.dp).background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = MaestroColors.White, fontWeight = FontWeight.Bold, fontSize = (size * 0.38f).sp)
    }
}

@Composable
fun MaestroSidebar(
    currentRoute: String,
    navController: NavHostController,
    userName: String = ""
) {
    Column(
        modifier = Modifier
            .width(248.dp)
            .fillMaxHeight()
            .background(MaestroColors.Cream)
    ) {
        Box(modifier = Modifier.fillMaxHeight().weight(1f)) {
            // Right border
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
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaestroColors.Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("𝄞", fontSize = 20.sp, color = MaestroColors.Espresso)
                    }
                    Text(
                        "MAESTRO",
                        color = MaestroColors.Espresso,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp
                    )
                }

                // Pentagram divider
                Pentagram(modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))

                // Nav items
                navTabs.forEach { tab ->
                    val selected = currentRoute == tab.screen.route ||
                        (tab.screen == Screen.Students && currentRoute.startsWith("student"))
                    SidebarNavItem(tab = tab, selected = selected) {
                        navController.navigate(tab.screen.route) { launchSingleTop = true }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // User profile footer
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 0.dp),
                    color = MaestroColors.LightGold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val displayName = if (userName.isNotBlank()) userName else "Profesor"
                    InitialsAvatar(displayName, size = 36)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.Espresso)
                        Text("Profesora · Piano", fontSize = 11.sp, color = MaestroColors.Muted)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null,
                        tint = MaestroColors.Muted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SidebarNavItem(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 1.dp)) {
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
                    if (selected) MaestroColors.White else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = if (selected) MaestroColors.Terra else MaestroColors.Muted,
                modifier = Modifier.size(18.dp)
            )
            Text(
                tab.label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaestroColors.Espresso else MaestroColors.Muted
            )
            if (selected) {
                Spacer(Modifier.weight(1f))
                Text("·", fontSize = 16.sp, color = MaestroColors.Gold, fontStyle = FontStyle.Normal)
            }
        }
    }
}

// Expansive header — only Dashboard
@Composable
fun MaestroExpansiveHeader(
    userName: String,
    dateLabel: String,
    subtitle: String,
    onNewClass: () -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaestroColors.Espresso)
        ) {
            // Decorative pentagram watermark
            Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 20.dp, top = 20.dp)) {
                Canvas(modifier = Modifier.width(280.dp).height(60.dp)) {
                    val color = Color(0xFFC9A84C).copy(alpha = 0.10f)
                    listOf(0f, 10f, 20f, 30f, 40f).forEach { y ->
                        drawLine(color, Offset(0f, y.dp.toPx()), Offset(size.width, y.dp.toPx()), 0.8.dp.toPx())
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date line
                Text(
                    dateLabel.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaestroColors.Gold,
                    letterSpacing = 1.4.sp
                )

                // Greeting + actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val firstName = userName.split(" ").firstOrNull()?.takeIf { it.isNotBlank() }
                        val fraunces = frauncesFamily()
                        Row {
                            Text(
                                if (firstName != null) "Hola, " else "Hola.",
                                fontFamily = fraunces,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaestroColors.White,
                                lineHeight = 34.sp
                            )
                            if (firstName != null) {
                                Text(
                                    "$firstName.",
                                    fontFamily = fraunces,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic,
                                    color = MaestroColors.Gold,
                                    lineHeight = 34.sp
                                )
                            }
                        }
                        if (subtitle.isNotBlank()) {
                            Text(
                                subtitle,
                                fontSize = 13.sp,
                                color = MaestroColors.White.copy(alpha = 0.65f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {},
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaestroColors.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(MaestroColors.White.copy(alpha = 0.3f), MaestroColors.White.copy(alpha = 0.3f)))
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null,
                                modifier = Modifier.size(15.dp), tint = MaestroColors.White)
                            Spacer(Modifier.width(6.dp))
                            Text("Hoy", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaestroColors.White)
                        }

                        Button(
                            onClick = onNewClass,
                            colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Gold),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null,
                                modifier = Modifier.size(15.dp), tint = MaestroColors.Espresso)
                            Spacer(Modifier.width(6.dp))
                            Text("Nueva clase", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaestroColors.Espresso)
                        }
                    }
                }
            }
        }
        // Gold gradient hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(MaestroColors.Gold, MaestroColors.LightGold, MaestroColors.Gold.copy(alpha = 0.3f))
                    )
                )
        )
    }
}

// Compact header — all other screens
@Composable
fun MaestroCompactHeader(
    pageTitle: String,
    breadcrumb: String = "",
    subtitle: String = ""
) {
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(26.dp).background(MaestroColors.Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("𝄞", fontSize = 14.sp, color = MaestroColors.Espresso)
                }
                Text("MAESTRO", color = MaestroColors.Gold, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }

            // Vertical divider
            Box(modifier = Modifier.width(1.dp).height(22.dp).background(MaestroColors.Gold.copy(alpha = 0.4f)))

            // Breadcrumb + title
            Column {
                if (breadcrumb.isNotBlank()) {
                    Text(
                        breadcrumb.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaestroColors.White.copy(alpha = 0.5f),
                        letterSpacing = 0.8.sp
                    )
                }
                if (pageTitle.isNotEmpty()) {
                    Text(pageTitle, color = MaestroColors.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = MaestroColors.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }
        // Gold gradient hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(MaestroColors.Gold, MaestroColors.LightGold, MaestroColors.Gold.copy(alpha = 0.3f))
                    )
                )
        )
    }
}

// Date helpers — pure Kotlin, no java.time
private fun dayOfWeek(year: Int, month: Int, day: Int): Int {
    val m = if (month <= 2) month + 12 else month
    val y = if (month <= 2) year - 1 else year
    val k = y % 100; val j = y / 100
    val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 - 2 * j) % 7
    return ((h + 5) % 7 + 7) % 7  // 0=Mon … 6=Sun
}

fun formatHeaderDate(isoDate: String): String {
    val parts = isoDate.split("-")
    val year = parts.getOrNull(0)?.toIntOrNull() ?: return ""
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return ""
    val day = parts.getOrNull(2)?.toIntOrNull() ?: return ""
    val dayNames = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")
    val monthNames = listOf("ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
        "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE")
    val dayName = dayNames.getOrElse(dayOfWeek(year, month, day)) { "LUNES" }
    val monthName = monthNames.getOrElse(month - 1) { "?" }
    return "$dayName · $day $monthName"
}

@Composable
fun AppScaffold(
    currentRoute: String,
    navController: NavHostController,
    pageTitle: String = "",
    breadcrumb: String = "",
    subtitle: String = "",
    userName: String = "",
    isDashboard: Boolean = false,
    dashboardHeader: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        MaestroSidebar(currentRoute, navController, userName)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaestroColors.Cream)
        ) {
            if (dashboardHeader != null) {
                dashboardHeader()
            } else {
                MaestroCompactHeader(pageTitle, breadcrumb, subtitle)
            }
            Box(modifier = Modifier.weight(1f)) { content() }
        }
    }
}
