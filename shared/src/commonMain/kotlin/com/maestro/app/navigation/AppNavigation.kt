package com.maestro.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.maestro.app.auth.TokenStorage
import com.maestro.app.data.createRepository
import com.maestro.app.dev.USE_MOCK
import com.maestro.app.network.ApiClient
import com.maestro.app.network.apiBaseUrl
import com.maestro.app.ui.auth.AuthScreen
import com.maestro.app.ui.classes.ClassesScreen
import com.maestro.app.ui.dashboard.DashboardScreen
import com.maestro.app.ui.events.EventsScreen
import com.maestro.app.ui.finances.FinancesScreen
import com.maestro.app.ui.guide.GuideScreen
import com.maestro.app.ui.metronome.MetronomeScreen
import com.maestro.app.ui.students.StudentDetailScreen
import com.maestro.app.ui.students.StudentsScreen
import com.maestro.app.ui.tasks.TasksScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Students : Screen("students")
    object StudentDetail : Screen("students/{studentId}") {
        fun route(id: String) = "students/$id"
    }
    object Classes : Screen("classes")
    object Finances : Screen("finances")
    object Tasks : Screen("tasks")
    object Events : Screen("events")
    object Metronome : Screen("metronome")
    object Guide : Screen("guide")
}

@Composable
fun AppNavigation() {
    val tokenStorage = remember { TokenStorage() }
    val apiClient = remember { ApiClient(apiBaseUrl(), tokenStorage) }
    val repository = remember { createRepository(apiClient, tokenStorage) }
    val navController = rememberNavController()
    val startDestination = if (USE_MOCK || tokenStorage.getToken() != null) Screen.Dashboard.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(apiClient, tokenStorage) { navController.navigate(Screen.Dashboard.route) { popUpTo(0) } }
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(repository, navController, if (USE_MOCK) "Sofía" else tokenStorage.getUserName() ?: "")
        }
        composable(Screen.Students.route) {
            StudentsScreen(repository, navController)
        }
        composable(
            Screen.StudentDetail.route,
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStack ->
            val studentId = backStack.arguments?.read { getString("studentId") } ?: ""
            StudentDetailScreen(studentId, repository, navController)
        }
        composable(Screen.Classes.route) { ClassesScreen(repository, navController) }
        composable(Screen.Finances.route) { FinancesScreen(repository, navController) }
        composable(Screen.Tasks.route) { TasksScreen(repository, navController) }
        composable(Screen.Events.route) { EventsScreen(repository, navController) }
        composable(Screen.Metronome.route) { MetronomeScreen(navController) }
        composable(Screen.Guide.route) { GuideScreen(navController) }
    }
}
