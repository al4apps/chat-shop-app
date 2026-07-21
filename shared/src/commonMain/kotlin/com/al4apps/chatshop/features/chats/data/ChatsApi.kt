package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

/** Действия фичи чатов (чтение — в SyncApi). */
class ChatsApi(
    private val client: HttpClient,
) {

    /** `POST /dialogs` — создаёт новый диалог (CLIENT_GUIDE §11, статус NEW). */
    suspend fun createDialog(): AppResult<DialogDto> = apiCall {
        client.post("dialogs")
    }

    /**
     * Отправка TEXT-сообщения. Идемпотентна по `clientMessageId`:
     * повтор с тем же id возвращает уже созданное сообщение без дубля.
     */
    suspend fun sendTextMessage(
        dialogId: String,
        request: SendMessageRequest,
    ): AppResult<MessageDto> = apiCall {
        client.post("dialogs/$dialogId/messages") {
            setBody(request)
        }
    }
}
