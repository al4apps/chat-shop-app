package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.OrderCommandRequest
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest

/**
 * Действия фичи чатов (чтение — в SyncApi). Реализации: сетевая ([DefaultChatsApi])
 * и тестовая ([TestChatsApi]); выбор реализации — в DI (`DiConfig.TEST_MODE`).
 */
interface ChatsApi {

    /** `POST /dialogs` — создаёт новый диалог (CLIENT_GUIDE §11, статус NEW). */
    suspend fun createDialog(): AppResult<DialogDto>

    /**
     * Отправка TEXT-сообщения. Идемпотентна по `clientMessageId`:
     * повтор с тем же id возвращает уже созданное сообщение без дубля.
     */
    suspend fun sendTextMessage(
        dialogId: String,
        request: SendMessageRequest,
    ): AppResult<MessageDto>

    /**
     * Загрузка изображения (multipart, CLIENT_GUIDE §8). Идемпотентна по [clientMessageId].
     */
    suspend fun sendImage(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        clientMessageId: String,
    ): AppResult<MessageDto>

    /**
     * Загрузка голосового (multipart, CLIENT_GUIDE §8): type=VOICE, durationSec (1..120).
     * Идемпотентна по [clientMessageId].
     */
    suspend fun sendVoice(
        dialogId: String,
        bytes: ByteArray,
        mime: String,
        fileName: String,
        durationSec: Int,
        clientMessageId: String,
    ): AppResult<MessageDto>

    /**
     * Команда заказа/диалога. Клиенту доступна только `ORDER_COMPLETED` из `PROCESSING`;
     * ответ — обновлённый снапшот диалога, само SYSTEM-событие придёт из ленты.
     */
    suspend fun sendOrderCommand(
        dialogId: String,
        request: OrderCommandRequest,
    ): AppResult<DialogDto>
}
