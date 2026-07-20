package com.al4apps.chatshop.services.sync

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.features.chats.data.dto.DialogsPageDto
import com.al4apps.chatshop.features.chats.data.dto.MessagesPageDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Чтение данных для синхронизации (CLIENT_GUIDE §5). */
class SyncApi(
    private val client: HttpClient,
) {

    suspend fun getDialogs(limit: Int = DIALOGS_LIMIT): AppResult<DialogsPageDto> = apiCall {
        client.get("dialogs") {
            parameter("limit", limit)
        }
    }

    suspend fun getMessages(
        dialogId: String,
        afterSeq: Long,
        limit: Int = MESSAGES_PAGE_LIMIT,
    ): AppResult<MessagesPageDto> = apiCall {
        client.get("dialogs/$dialogId/messages") {
            parameter("afterSeq", afterSeq)
            parameter("limit", limit)
        }
    }

    companion object {
        const val DIALOGS_LIMIT = 50
        const val MESSAGES_PAGE_LIMIT = 100
    }
}
