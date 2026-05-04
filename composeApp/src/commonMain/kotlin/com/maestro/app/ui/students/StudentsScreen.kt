package com.maestro.app.ui.students

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.maestro.app.network.ApiClient

@Composable
fun StudentsScreen(apiClient: ApiClient, navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Students – coming soon")
    }
}

@Composable
fun StudentDetailScreen(studentId: Long, apiClient: ApiClient, navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Student Detail – coming soon")
    }
}
