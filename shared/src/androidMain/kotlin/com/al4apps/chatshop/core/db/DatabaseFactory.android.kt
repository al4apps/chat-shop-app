package com.al4apps.chatshop.core.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.al4apps.chatshop.db.ChatShopDatabase

actual class DatabaseFactory(
    private val context: Context,
) {

    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = ChatShopDatabase.Schema,
        context = context,
        name = DATABASE_NAME,
    )
}
