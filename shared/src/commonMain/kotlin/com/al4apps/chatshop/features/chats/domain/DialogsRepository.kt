package com.al4apps.chatshop.features.chats.domain

import com.al4apps.chatshop.features.chats.domain.model.Dialog
import kotlinx.coroutines.flow.Flow

/** Диалоги. Единственный источник истины для UI — локальная БД. */
interface DialogsRepository {

    /**
     * Поток всех диалогов, отсортированных по последней активности (убыв.).
     * Эмитит при любом изменении таблицы.
     */
    fun observeDialogs(): Flow<List<Dialog>>

    /**
     * Поток одного диалога.
     *
     * @return null в потоке, если диалога нет в БД.
     */
    fun observeDialog(id: String): Flow<Dialog?>

    /** Есть ли хотя бы один диалог (роутинг: первый заказ vs список чатов). */
    suspend fun hasDialogs(): Boolean

    /**
     * Транзакционно применяет серверные снапшоты диалогов (проекция заказа,
     * статус, activity). Локальный курсор поллинга при этом не затирается.
     */
    suspend fun upsertFromServer(dialogs: List<Dialog>)

    /**
     * Курсор последнего полученного сообщения диалога (`lastSeq` поллинга).
     *
     * @return 0, если диалог ещё не синхронизировался.
     */
    suspend fun lastSyncedSeq(dialogId: String): Long

    /** Продвигает курсор поллинга вперёд; движение назад игнорируется. */
    suspend fun advanceLastSyncedSeq(dialogId: String, seq: Long)
}
