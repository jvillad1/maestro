package com.maestro.app.data

import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.shared.repository.MaestroRepository

/**
 * wasm: the server serves the app, so the repository is the ApiClient itself.
 * Android: offline-first LocalRepository over SQLDelight + background SyncEngine.
 */
expect fun createRepository(apiClient: ApiClient, tokenStorage: TokenStorage): MaestroRepository
