package com.al4apps.chatshop.features.chats.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.AttachedFile
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.SendState
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
            remoteUrl = file.remoteUrl,
            updatedAt = updatedAt,
        )
        db.filesQueries.updateFromServer(
            id = file.id,
            mime = file.mime,
            size = file.size,
            durationSec = file.durationSec?.toLong(),
            status = file.status.wire,
            remoteUrl = file.remoteUrl,
            updatedAt = updatedAt,
        )
    }

    override suspend fun enqueueText(
        dialogId: String,
        clientMessageId: String,
        text: String,
    ) {
        withContext(ioDispatcher) {
            db.messagesQueries.insertOutgoingText(
                dialogId = dialogId,
                clientMessageId = clientMessageId,
                text = text,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        }
    }

    override suspend fun confirmSent(localId: Long, serverMessage: Message) {
        withContext(ioDispatcher) {
            val now = Clock.System.now().toEpochMilliseconds()
            db.messagesQueries.transaction {
                serverMessage.file?.let { file -> upsertFile(file, now) }
                val seq = serverMessage.seq
                val existing = seq?.let {
                    db.messagesQueries.selectLocalIdBySeq(it).executeAsOneOrNull()
                }
                if (existing != null && existing != localId) {
                    // Поллинг уже создал строку с этим seq — убираем оптимистичную.
                    db.messagesQueries.deleteByLocalId(localId)
                } else {
                    db.messagesQueries.confirmSentByLocalId(
                        seq = seq,
                        senderRole = serverMessage.senderRole.wire,
                        type = serverMessage.type.wire,
                        text = serverMessage.text,
                        eventType = serverMessage.eventType,
                        payload = serverMessage.payload,
                        createdAt = serverMessage.createdAt.toEpochMilliseconds(),
                        fileId = serverMessage.file?.id,
                        localId = localId,
                    )
                }
            }
        }
    }

    override suspend fun enqueueImage(
        dialogId: String,
        clientMessageId: String,
        fileId: String,
        mime: String,
        size: Long,
        localPath: String,
    ) {
        withContext(ioDispatcher) {
            val now = Clock.System.now().toEpochMilliseconds()
            db.messagesQueries.transaction {
                db.filesQueries.insertLocal(
                    id = fileId,
                    mime = mime,
                    size = size,
                    durationSec = null,
                    localPath = localPath,
                    updatedAt = now,
                )
                db.messagesQueries.insertOutgoingImage(
                    dialogId = dialogId,
                    clientMessageId = clientMessageId,
                    createdAt = now,
                    fileId = fileId,
                )
            }
        }
    }

    override suspend fun enqueueVoice(
        dialogId: String,
        clientMessageId: String,
        fileId: String,
        mime: String,
        size: Long,
        durationSec: Int,
        localPath: String,
    ) {
        withContext(ioDispatcher) {
            val now = Clock.System.now().toEpochMilliseconds()
            db.messagesQueries.transaction {
                db.filesQueries.insertLocal(
                    id = fileId,
                    mime = mime,
                    size = size,
                    durationSec = durationSec.toLong(),
                    localPath = localPath,
                    updatedAt = now,
                )
                db.messagesQueries.insertOutgoingVoice(
                    dialogId = dialogId,
                    clientMessageId = clientMessageId,
                    createdAt = now,
                    fileId = fileId,
                )
            }
        }
    }

    override fun observePending(): Flow<List<Message>> =
        db.messagesQueries.selectPending()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun markSending(localId: Long) {
        withContext(ioDispatcher) {
            db.messagesQueries.updateSendState(
                sendState = SendState.SENDING.name,
                localId = localId,
            )
        }
    }

    override suspend fun markFailed(localId: Long) {
        withContext(ioDispatcher) {
            db.messagesQueries.updateSendState(
                sendState = SendState.FAILED.name,
                localId = localId,
            )
        }
    }

    override suspend fun retry(localId: Long) {
        withContext(ioDispatcher) {
            db.messagesQueries.retryFailed(localId)
        }
    }

    override suspend fun resetStuckSending() {
        withContext(ioDispatcher) {
            db.messagesQueries.resetStuckSending()
        }
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
