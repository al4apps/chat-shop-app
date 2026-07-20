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
}
