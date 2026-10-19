package com.al4apps.chatshop.services.testmode

import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.chats.data.test.TestChatsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Подготовка тестовых данных на старте: очищает БД от прошлых данных и
 * сбрасывает in-memory мок-набор. Создаётся только в тестовом DI-модуле.
 */
class TestDataInitializer(
    private val db: ChatShopDatabase,
    private val store: TestChatsStore,
    private val scope: CoroutineScope,
) {

    init {
        scope.launch {
            withContext(AppDispatchers.io) {
                db.transaction {
                    db.messagesQueries.deleteAll()
                    db.filesQueries.deleteAll()
                    db.dialogsQueries.deleteAll()
                }
            }
            store.reset()
        }
    }
}
