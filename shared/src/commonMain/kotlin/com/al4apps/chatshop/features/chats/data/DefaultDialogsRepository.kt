package com.al4apps.chatshop.features.chats.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultDialogsRepository(
    private val db: ChatShopDatabase,
    private val ioDispatcher: CoroutineDispatcher = AppDispatchers.io,
) : DialogsRepository {

    override fun observeDialogs(): Flow<List<Dialog>> =
        db.dialogsQueries.selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeDialog(id: String): Flow<Dialog?> =
        db.dialogsQueries.selectById(id)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map { row -> row?.toDomain() }

    override suspend fun hasDialogs(): Boolean = withContext(ioDispatcher) {
        db.dialogsQueries.countAll().executeAsOne() > 0
    }

    override suspend fun upsertFromServer(dialogs: List<Dialog>) = withContext(ioDispatcher) {
        db.dialogsQueries.transaction {
            dialogs.forEach { dialog ->
                db.dialogsQueries.insertOrIgnore(
                    id = dialog.id,
                    status = dialog.status.wire,
                    orderStatus = dialog.orderStatus?.wire,
                    orderAmount = dialog.orderAmount?.asDecimalString(),
                    orderDescription = dialog.orderDescription,
                    orderNo = dialog.orderNo.toLong(),
                    lastMessageAt = dialog.lastMessageAt?.toEpochMilliseconds(),
                    lastMessageSeq = dialog.lastMessageSeq,
                    createdAt = dialog.createdAt.toEpochMilliseconds(),
                    hasOperator = if (dialog.hasOperator) 1L else 0L,
                )
                db.dialogsQueries.updateFromServer(
                    id = dialog.id,
                    status = dialog.status.wire,
                    orderStatus = dialog.orderStatus?.wire,
                    orderAmount = dialog.orderAmount?.asDecimalString(),
                    orderDescription = dialog.orderDescription,
                    orderNo = dialog.orderNo.toLong(),
                    lastMessageAt = dialog.lastMessageAt?.toEpochMilliseconds(),
                    lastMessageSeq = dialog.lastMessageSeq,
                    hasOperator = if (dialog.hasOperator) 1L else 0L,
                )
            }
        }
    }

    override suspend fun lastSyncedSeq(dialogId: String): Long = withContext(ioDispatcher) {
        db.dialogsQueries.selectLastSyncedSeq(dialogId).executeAsOneOrNull() ?: 0L
    }

    override suspend fun advanceLastSyncedSeq(dialogId: String, seq: Long) {
        withContext(ioDispatcher) {
            db.dialogsQueries.advanceLastSyncedSeq(seq = seq, id = dialogId)
        }
    }
}
