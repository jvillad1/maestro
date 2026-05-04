package com.maestro.app

import androidx.compose.runtime.Composable
import com.maestro.app.navigation.AppNavigation
import com.maestro.app.theme.MaestroTheme

@Composable
fun App() {
    MaestroTheme {
        AppNavigation()
    }
}
