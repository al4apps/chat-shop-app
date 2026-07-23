package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

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

    /**
     * Загрузка изображения (multipart, CLIENT_GUIDE §8). Идемпотентна по [clientMessageId].
     */
    suspend fun sendImage(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        clientMessageId: String,
    ): AppResult<MessageDto> = apiCall {
        client.submitFormWithBinaryData(
            url = "dialogs/$dialogId/files",
            formData = formData {
                append("type", "IMAGE")
                append("clientMessageId", clientMessageId)
                append(
                    key = "file",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mime)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    },
                )
            },
        )
    }
}
