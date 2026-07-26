package com.al4apps.chatshop.features.chats.domain

import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlinx.coroutines.flow.Flow

/** Лента сообщений. Единственный источник истины для UI — локальная БД. */
interface MessagesRepository {

    /**
     * Поток ленты диалога: серверные сообщения по возрастанию `seq`,
     * ниже — локальные неотправленные по порядку создания. Вложения включены.
     */
    fun observeMessages(dialogId: String): Flow<List<Message>>

    /**
     * Транзакционно применяет серверные сообщения:
     * - сообщение с нашим `clientMessageId` заменяет локальную строку (та получает seq);
     * - остальные — upsert по `seq` (payload/файл могут обновляться);
     * - метаданные вложений сохраняются в таблицу файлов (без затирания пути кэша).
     */
    suspend fun upsertFromServer(messages: List<Message>)

    /**
     * Кладёт исходящий TEXT в очередь отправки (`sendState=PENDING`).
     * UI мгновенно видит сообщение из БД; доставку выполняет OutboxService.
     */
    suspend fun enqueueText(dialogId: String, clientMessageId: String, text: String)

    /**
     * Кладёт исходящее IMAGE в очередь отправки. Байты уже сохранены в FileStore под
     * [fileId]; создаётся локальная запись файла и сообщение `type=IMAGE, PENDING`.
     */
    suspend fun enqueueImage(
        dialogId: String,
        clientMessageId: String,
        fileId: String,
        mime: String,
        size: Long,
        localPath: String,
    )

    /**
     * Кладёт исходящее VOICE в очередь отправки. Байты уже в FileStore под [fileId];
     * [durationSec] — реальная длительность записи (1..120, CLIENT_GUIDE §8).
     */
    suspend fun enqueueVoice(
        dialogId: String,
        clientMessageId: String,
        fileId: String,
        mime: String,
        size: Long,
        durationSec: Int,
        localPath: String,
    )

    /**
     * Подтверждает доставку: оптимистичная строка [localId] получает данные сервера
     * (seq, SYNCED). Сопоставление по `localId`, а не по clientMessageId — не зависит
     * от того, эхает ли сервер clientMessageId. Если поллинг уже создал строку с этим
     * `seq`, оптимистичная строка удаляется (каноничной остаётся серверная). Дублей нет.
     */
    suspend fun confirmSent(localId: Long, serverMessage: Message)

    /** Поток очереди отправки (PENDING) в порядке создания. */
    fun observePending(): Flow<List<Message>>

    /** Помечает сообщение отправляемым (SENDING). */
    suspend fun markSending(localId: Long)

    /** Помечает отправку неуспешной (FAILED, доступен ручной повтор). */
    suspend fun markFailed(localId: Long)

    /** Возвращает FAILED-сообщение в очередь (тот же clientMessageId — без дублей). */
    suspend fun retry(localId: Long)

    /** Возвращает застрявшие SENDING в очередь (вызов на старте процесса). */
    suspend fun resetStuckSending()
}
