package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.network.apiCall
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.OrderCommandRequest
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/** Сетевая реализация действий фичи чатов. */
class DefaultChatsApi(
    private val client: HttpClient,
) : ChatsApi {

    override suspend fun createDialog(): AppResult<DialogDto> = apiCall {
        client.post("dialogs")
    }

    override suspend fun sendTextMessage(
        dialogId: String,
        request: SendMessageRequest,
    ): AppResult<MessageDto> = apiCall {
        client.post("dialogs/$dialogId/messages") {
            setBody(request)
        }
    }

    override suspend fun sendImage(
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

    override suspend fun sendVoice(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        durationSec: Int,
        clientMessageId: String,
    ): AppResult<MessageDto> = apiCall {
        client.submitFormWithBinaryData(
            url = "dialogs/$dialogId/files",
            formData = formData {
                append("type", "VOICE")
                append("durationSec", durationSec.toString())
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

    override suspend fun sendOrderCommand(
        dialogId: String,
        request: OrderCommandRequest,
    ): AppResult<DialogDto> = apiCall {
        client.post("dialogs/$dialogId/commands") {
            setBody(request)
        }
    }
}
