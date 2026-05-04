package com.maestro.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.shared.dto.LoginRequest
import com.maestro.shared.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRegistering: Boolean = false
)

class AuthViewModel(
    private val apiClient: ApiClient,
    private val tokenStorage: TokenStorage
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = apiClient.login(LoginRequest(email, password))
                tokenStorage.saveToken(response.token)
                tokenStorage.saveUserName(response.user.name)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Credenciales incorrectas", isLoading = false)
            }
        }
    }

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = apiClient.register(RegisterRequest(email, password, name))
                tokenStorage.saveToken(response.token)
                tokenStorage.saveUserName(response.user.name)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error al registrar. Intenta de nuevo.", isLoading = false)
            }
        }
    }

    fun toggleMode() { _state.value = _state.value.copy(isRegistering = !_state.value.isRegistering, error = null) }
}
