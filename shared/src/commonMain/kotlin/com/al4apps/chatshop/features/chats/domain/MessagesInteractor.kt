package com.al4apps.chatshop.features.chats.domain

import com.al4apps.chatshop.core.media.PickedImage
import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlinx.coroutines.flow.Flow

/** Сценарии ленты сообщений. */
interface MessagesInteractor {

    /**
     * Лента диалога из локальной БД (единственный источник истины):
     * серверные по возрастанию seq, ниже — неотправленные локальные.
     */
    fun observeMessages(dialogId: String): Flow<List<Message>>

    /**
     * Отправляет текст: генерирует `clientMessageId` и кладёт сообщение
     * в очередь отправки. В ленте появляется мгновенно (PENDING);
     * доставку выполняет OutboxService, ретраи — тем же id (без дублей).
     */
    suspend fun sendText(dialogId: String, text: String)

    /**
     * Отправляет изображение: сжимает в JPEG ≤5 МБ, сохраняет в локальный кэш и кладёт
     * в очередь отправки. В ленте появляется мгновенно; доставку выполняет OutboxService.
     */
    suspend fun sendImage(dialogId: String, image: PickedImage)

    /** Возвращает неотправленное (FAILED) сообщение в очередь отправки. */
    suspend fun retryMessage(localId: Long)
}
