package com.maestro.shared.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun createAndroidDriver(context: Context): SqlDriver =
    AndroidSqliteDriver(MaestroDatabase.Schema, context, "maestro.db")
