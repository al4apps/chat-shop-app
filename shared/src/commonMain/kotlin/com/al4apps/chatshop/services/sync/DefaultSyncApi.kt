package com.al4apps.chatshop.services.sync

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.features.chats.data.dto.DialogsPageDto
import com.al4apps.chatshop.features.chats.data.dto.MessagesPageDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/** Сетевая реализация чтения данных синхронизации. */
class DefaultSyncApi(
    private val client: HttpClient,
) : SyncApi {

    override suspend fun getDialogs(limit: Int): AppResult<DialogsPageDto> = apiCall {
        client.get("dialogs") {
            parameter("limit", limit)
        }
    }

    override suspend fun getMessages(
        dialogId: String,
        afterSeq: Long,
        limit: Int,
    ): AppResult<MessagesPageDto> = apiCall {
        client.get("dialogs/$dialogId/messages") {
            parameter("afterSeq", afterSeq)
            parameter("limit", limit)
        }
    }
}
