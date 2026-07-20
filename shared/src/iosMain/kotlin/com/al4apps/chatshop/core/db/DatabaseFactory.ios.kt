package com.al4apps.chatshop.core.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.al4apps.chatshop.db.ChatShopDatabase

actual class DatabaseFactory {

    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = ChatShopDatabase.Schema,
        name = DATABASE_NAME,
    )
}
