package com.al4apps.chatshop.core.db

import app.cash.sqldelight.db.SqlDriver

/** Платформенная фабрика драйвера SQLite. Создаётся на краю приложения (Koin platformModule). */
expect class DatabaseFactory {

    fun createDriver(): SqlDriver
}

const val DATABASE_NAME = "chatshop.db"
