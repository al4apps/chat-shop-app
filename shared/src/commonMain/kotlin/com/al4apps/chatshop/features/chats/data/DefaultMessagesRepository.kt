package com.al4apps.chatshop.features.chats.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.AttachedFile
import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTime::class)
class DefaultMessagesRepository(
    private val db: ChatShopDatabase,
    private val ioDispatcher: CoroutineDispatcher = AppDispatchers.io,
) : MessagesRepository {

    override fun observeMessages(dialogId: String): Flow<List<Message>> =
        db.messagesQueries.selectByDialog(dialogId)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun upsertFromServer(messages: List<Message>) = withContext(ioDispatcher) {
        val now = Clock.System.now().toEpochMilliseconds()
        db.messagesQueries.transaction {
            messages.forEach { message ->
                message.file?.let { file -> upsertFile(file, now) }
                upsertMessage(message)
            }
        }
    }

    private fun upsertFile(file: AttachedFile, updatedAt: Long) {
        db.filesQueries.insertOrIgnore(
            id = file.id,
            mime = file.mime,
            size = file.size,
            durationSec = file.durationSec?.toLong(),
            status = file.status.wire,
            updatedAt = updatedAt,
        )
        db.filesQueries.updateFromServer(
            id = file.id,
            mime = file.mime,
            size = file.size,
            durationSec = file.durationSec?.toLong(),
            status = file.status.wire,
            updatedAt = updatedAt,
        )
    }

    private fun upsertMessage(message: Message) {
        val localId = message.clientMessageId?.let { clientMessageId ->
            db.messagesQueries.selectLocalIdByClientMessageId(clientMessageId)
                .executeAsOneOrNull()
        }
        if (localId != null) {
            db.messagesQueries.updateFromServerByClientMessageId(
                seq = message.seq,
                senderRole = message.senderRole.wire,
                type = message.type.wire,
                text = message.text,
                eventType = message.eventType,
                payload = message.payload,
                createdAt = message.createdAt.toEpochMilliseconds(),
                fileId = message.file?.id,
                clientMessageId = message.clientMessageId,
            )
        } else {
            db.messagesQueries.insertIgnoreBySeq(
                seq = message.seq,
                dialogId = message.dialogId,
                clientMessageId = message.clientMessageId,
                senderRole = message.senderRole.wire,
                type = message.type.wire,
                text = message.text,
                eventType = message.eventType,
                payload = message.payload,
                createdAt = message.createdAt.toEpochMilliseconds(),
                fileId = message.file?.id,
            )
            db.messagesQueries.updateBySeq(
                seq = message.seq,
                text = message.text,
                eventType = message.eventType,
                payload = message.payload,
                fileId = message.file?.id,
            )
        }
    }
}
