package com.al4apps.chatshop.features.chats.domain

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import kotlinx.coroutines.flow.Flow

/** Сценарии работы с диалогами. */
interface ChatsInteractor {

    /**
     * Поток диалогов из локальной БД (единственный источник истины для UI),
     * отсортированных по последней активности.
     */
    fun observeDialogs(): Flow<List<Dialog>>

    /** Есть ли хотя бы один диалог локально (роутинг: первый заказ vs список). */
    suspend fun hasDialogs(): Boolean

    /**
     * Создаёт новый диалог.
     *
     * Поведение: `POST /dialogs`; успешный ответ кэшируется в БД до возврата.
     *
     * @return id созданного диалога; ошибки: сеть, сервер (problem+json).
     */
    suspend fun createDialog(): AppResult<String>
}
