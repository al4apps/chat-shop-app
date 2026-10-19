package com.al4apps.chatshop.services.sync.test

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.dto.DialogsPageDto
import com.al4apps.chatshop.features.chats.data.dto.MessagesPageDto
import com.al4apps.chatshop.features.chats.data.test.TestChatsStore
import com.al4apps.chatshop.services.sync.SyncApi

/** Мок чтения: отдаёт диалоги и ленту из in-memory [TestChatsStore] без сети. */
class TestSyncApi(
    private val store: TestChatsStore,
) : SyncApi {

    override suspend fun getDialogs(limit: Int): AppResult<DialogsPageDto> =
        AppResult.Success(DialogsPageDto(items = store.dialogs().take(limit)))

    override suspend fun getMessages(
        dialogId: String,
        afterSeq: Long,
        limit: Int,
    ): AppResult<MessagesPageDto> = AppResult.Success(
        store.messages(dialogId = dialogId, afterSeq = afterSeq, limit = limit),
    )
}
