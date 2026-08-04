package com.maestro.app.data

import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.shared.repository.MaestroRepository

actual fun createRepository(apiClient: ApiClient, tokenStorage: TokenStorage): MaestroRepository =
    apiClient
