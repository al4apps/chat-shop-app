package com.al4apps.chatshop.services.sync

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.dto.DialogsPageDto
import com.al4apps.chatshop.features.chats.data.dto.MessagesPageDto

/**
 * Чтение данных для синхронизации (CLIENT_GUIDE §5). Реализации: сетевая ([DefaultSyncApi])
 * и тестовая ([TestSyncApi]); выбор реализации — в DI (`DiConfig.TEST_MODE`).
 */
interface SyncApi {

    suspend fun getDialogs(limit: Int = DIALOGS_LIMIT): AppResult<DialogsPageDto>

    suspend fun getMessages(
        dialogId: String,
        afterSeq: Long,
        limit: Int = MESSAGES_PAGE_LIMIT,
    ): AppResult<MessagesPageDto>

    companion object {
        const val DIALOGS_LIMIT = 50
        const val MESSAGES_PAGE_LIMIT = 100
    }
}
