package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post

/** Действия фичи чатов (чтение — в SyncApi). */
class ChatsApi(
    private val client: HttpClient,
) {

    /** `POST /dialogs` — создаёт новый диалог (CLIENT_GUIDE §11, статус NEW). */
    suspend fun createDialog(): AppResult<DialogDto> = apiCall {
        client.post("dialogs")
    }
}
