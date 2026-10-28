package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.ChatsApi
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.OrderCommandRequest
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import com.al4apps.chatshop.features.chats.domain.model.MessageType

/** Управляемый `ChatsApi`: записывает вызовы и отдаёт заданный результат. */
class FakeOutboxChatsApi(
    private val handler: suspend (OutboxApiCall) -> AppResult<MessageDto> = { call ->
        AppResult.Success(serverMessage(call))
    },
) : ChatsApi {

    val calls: MutableList<OutboxApiCall> = mutableListOf()

    override suspend fun createDialog(): AppResult<DialogDto> =
        AppResult.Failure(AppError.Unknown)

    override suspend fun sendTextMessage(
        dialogId: String,
        request: SendMessageRequest,
    ): AppResult<MessageDto> = record(
        OutboxApiCall(
            dialogId = dialogId,
            clientMessageId = request.clientMessageId,
            type = MessageType.TEXT,
            text = request.text,
        ),
    )

    override suspend fun sendImage(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        clientMessageId: String,
    ): AppResult<MessageDto> = record(
        OutboxApiCall(
            dialogId = dialogId,
            clientMessageId = clientMessageId,
            type = MessageType.IMAGE,
            bytes = bytes,
            mime = mime,
        ),
    )

    override suspend fun sendVoice(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        durationSec: Int,
        clientMessageId: String,
    ): AppResult<MessageDto> = record(
        OutboxApiCall(
            dialogId = dialogId,
            clientMessageId = clientMessageId,
            type = MessageType.VOICE,
            bytes = bytes,
            mime = mime,
            durationSec = durationSec,
        ),
    )

    override suspend fun sendOrderCommand(
        dialogId: String,
        request: OrderCommandRequest,
    ): AppResult<DialogDto> = AppResult.Failure(AppError.Unknown)

    private suspend fun record(call: OutboxApiCall): AppResult<MessageDto> {
        calls += call
        return handler(call)
    }
}
