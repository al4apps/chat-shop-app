package com.al4apps.chatshop.features.chats.data.test

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.ChatsApi
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest

/**
 * Мок действий фичи чатов: создание диалога и отправка сообщений «уходят» в
 * in-memory [TestChatsStore] и возвращаются как серверные без сети.
 */
class TestChatsApi(
    private val store: TestChatsStore,
) : ChatsApi {

    override suspend fun createDialog(): AppResult<DialogDto> =
        AppResult.Success(store.createDialog())

    override suspend fun sendTextMessage(
        dialogId: String,
        request: SendMessageRequest,
    ): AppResult<MessageDto> = AppResult.Success(
        store.addClientText(
            dialogId = dialogId,
            text = request.text,
            clientMessageId = request.clientMessageId,
        ),
    )

    override suspend fun sendImage(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        clientMessageId: String,
    ): AppResult<MessageDto> = AppResult.Success(
        store.addClientFile(
            dialogId = dialogId,
            type = "IMAGE",
            mime = mime,
            size = bytes.size.toLong(),
            durationSec = null,
            clientMessageId = clientMessageId,
        ),
    )

    override suspend fun sendVoice(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        durationSec: Int,
        clientMessageId: String,
    ): AppResult<MessageDto> = AppResult.Success(
        store.addClientFile(
            dialogId = dialogId,
            type = "VOICE",
            mime = mime,
            size = bytes.size.toLong(),
            durationSec = durationSec,
            clientMessageId = clientMessageId,
        ),
    )
}
