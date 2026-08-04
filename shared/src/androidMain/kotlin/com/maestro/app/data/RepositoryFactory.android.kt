package com.maestro.app.data

import com.maestro.app.AppContextHolder
import com.maestro.app.auth.TokenStorage
import com.maestro.app.network.ApiClient
import com.maestro.shared.db.MaestroDatabase
import com.maestro.shared.db.createAndroidDriver
import com.maestro.shared.repository.LocalRepository
import com.maestro.shared.repository.MaestroRepository
import com.maestro.shared.repository.SyncEngine

actual fun createRepository(apiClient: ApiClient, tokenStorage: TokenStorage): MaestroRepository {
    val db = MaestroDatabase(createAndroidDriver(AppContextHolder.context))
    val userId = { tokenStorage.getUserId() ?: 0L }
    SyncEngine(db, apiClient, userId).start()
    return LocalRepository(db, apiClient, userId)
}
