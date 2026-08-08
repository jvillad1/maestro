package com.maestro.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.app.theme.MaestroColors
import com.maestro.shared.util.isValidEmail

@Composable
fun AuthScreen(apiClient: ApiClient, tokenStorage: TokenStorage, onSuccess: () -> Unit) {
    val vm = viewModel { AuthViewModel(apiClient, tokenStorage) }
    val state by vm.state.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    // Login form focus requesters: email -> password -> email (circular)
    val loginFocusEmail = remember { FocusRequester() }
    val loginFocusPassword = remember { FocusRequester() }

    // Register form focus requesters: name -> email -> password -> name (circular)
    val registerFocusName = remember { FocusRequester() }
    val registerFocusEmail = remember { FocusRequester() }
    val registerFocusPassword = remember { FocusRequester() }

    Box(
        modifier = Modifier.fillMaxSize().background(MaestroColors.Cream),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaestroColors.White)
        ) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("MAESTRO", style = MaterialTheme.typography.headlineMedium, color = MaestroColors.Espresso)
                Text(
                    if (state.isRegistering) "Crear cuenta" else "Iniciar sesión",
                    style = MaterialTheme.typography.titleMedium, color = MaestroColors.Muted
                )

                if (state.isRegistering) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(registerFocusName)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                    registerFocusEmail.requestFocus(); true
                                } else false
                            }
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        isError = email.isNotBlank() && !isValidEmail(email),
                        supportingText = if (email.isNotBlank() && !isValidEmail(email)) {
                            { Text("Email inválido") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(registerFocusEmail)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                    registerFocusPassword.requestFocus(); true
                                } else false
                            }
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(),
                        isError = password.isNotBlank() && password.length < 6,
                        supportingText = if (password.isNotBlank() && password.length < 6) {
                            { Text("Mínimo 6 caracteres") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(registerFocusPassword)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                    registerFocusName.requestFocus(); true
                                } else false
                            }
                    )
                } else {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(loginFocusEmail)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                    loginFocusPassword.requestFocus(); true
                                } else false
                            }
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(loginFocusPassword)
                            .onPreviewKeyEvent { e ->
                                if (e.key == Key.Tab && e.type == KeyEventType.KeyDown) {
                                    loginFocusEmail.requestFocus(); true
                                } else false
                            }
                    )
                }

                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = {
                        if (state.isRegistering) vm.register(email, password, name, onSuccess)
                        else vm.login(email, password, onSuccess)
                    },
                    enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank() &&
                        (!state.isRegistering || (name.isNotBlank() && isValidEmail(email) && password.length >= 6)),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaestroColors.Terra)
                ) {
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaestroColors.White)
                    else Text(if (state.isRegistering) "Registrarse" else "Entrar")
                }

                TextButton(onClick = { vm.toggleMode() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        if (state.isRegistering) "¿Ya tenés cuenta? Iniciá sesión" else "¿No tenés cuenta? Registrate",
                        color = MaestroColors.Gold
                    )
                }
            }
        }
    }
}
