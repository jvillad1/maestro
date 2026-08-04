package com.maestro.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

fun createIosDriver(): SqlDriver =
    NativeSqliteDriver(MaestroDatabase.Schema, "maestro.db")
