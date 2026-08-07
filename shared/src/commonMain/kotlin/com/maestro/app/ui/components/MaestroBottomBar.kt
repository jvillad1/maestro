package com.maestro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.maestro.app.navigation.Screen
import com.maestro.app.theme.MaestroColors

// Phone navigation: the 4 main sections live in the bottom bar,
// the rest are reachable through the "Más" sheet.
private val primaryTabs = navTabs.take(4)
private val overflowTabs = navTabs.drop(4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaestroBottomBar(
    currentRoute: String,
    navController: NavHostController,
    onLogout: () -> Unit = {}
) {
    var showMore by remember { mutableStateOf(false) }
    val overflowSelected = overflowTabs.any { it.screen.route == currentRoute }

    Column {
        HorizontalDivider(color = MaestroColors.LightGold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaestroColors.Cream)
                .navigationBarsPadding()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            primaryTabs.forEach { tab ->
                val selected = currentRoute == tab.screen.route ||
                    (tab.screen == Screen.Students && currentRoute.startsWith("student"))
                BottomBarItem(
                    label = tab.label,
                    icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(22.dp), tint = if (selected) MaestroColors.Terra else MaestroColors.Muted) },
                    selected = selected,
                    modifier = Modifier.weight(1f)
                ) {
                    navController.navigate(tab.screen.route) { launchSingleTop = true }
                }
            }
            BottomBarItem(
                label = "Más",
                icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Más", modifier = Modifier.size(22.dp), tint = if (overflowSelected) MaestroColors.Terra else MaestroColors.Muted) },
                selected = overflowSelected,
                modifier = Modifier.weight(1f)
            ) { showMore = true }
        }
    }

    if (showMore) {
        ModalBottomSheet(
            onDismissRequest = { showMore = false },
            containerColor = MaestroColors.Cream
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Pentagram(modifier = Modifier.padding(horizontal = 4.dp))
                Spacer(Modifier.height(10.dp))
                overflowTabs.forEach { tab ->
                    val selected = currentRoute == tab.screen.route
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selected) MaestroColors.White else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable {
                                showMore = false
                                navController.navigate(tab.screen.route) { launchSingleTop = true }
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(20.dp),
                            tint = if (selected) MaestroColors.Terra else MaestroColors.Muted)
                        Text(tab.label, fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) MaestroColors.Espresso else MaestroColors.Muted)
                        if (selected) {
                            Spacer(Modifier.weight(1f))
                            Box(Modifier.size(6.dp).background(MaestroColors.Gold, CircleShape))
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaestroColors.LightGold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMore = false
                            onLogout()
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión",
                        modifier = Modifier.size(20.dp), tint = MaestroColors.Terra)
                    Text("Cerrar sesión", fontSize = 15.sp, color = MaestroColors.Terra,
                        fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaestroColors.Espresso else MaestroColors.Muted
        )
    }
}
