package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.features.chats.data.dto.FileDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.domain.model.AttachedFile
import com.al4apps.chatshop.features.chats.domain.model.FileStatus
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import kotlinx.datetime.Instant

internal const val DIALOG_A = "dialog-a"
internal const val DIALOG_B = "dialog-b"

/** Исходящий TEXT в очереди outbox. */
internal fun outgoingText(
    localId: Long,
    dialogId: String,
    clientMessageId: String = "client-$localId",
    text: String = "text-$localId",
): Message = Message(
    localId = localId,
    seq = null,
    dialogId = dialogId,
    clientMessageId = clientMessageId,
    senderRole = SenderRole.CLIENT,
    type = MessageType.TEXT,
    text = text,
    eventType = null,
    payload = null,
    createdAt = Instant.fromEpochMilliseconds(localId),
    sendState = SendState.PENDING,
    file = null,
)

/** Исходящий IMAGE с локальной копией байтов. */
internal fun outgoingImage(
    localId: Long,
    dialogId: String,
    fileId: String,
    clientMessageId: String = "client-$localId",
    mime: String = "image/jpeg",
    size: Long = 3,
): Message = Message(
    localId = localId,
    seq = null,
    dialogId = dialogId,
    clientMessageId = clientMessageId,
    senderRole = SenderRole.CLIENT,
    type = MessageType.IMAGE,
    text = null,
    eventType = null,
    payload = null,
    createdAt = Instant.fromEpochMilliseconds(localId),
    sendState = SendState.PENDING,
    file = AttachedFile(
        id = fileId,
        mime = mime,
        size = size,
        durationSec = null,
        status = FileStatus.ACTIVE,
        localPath = "/local/$fileId",
    ),
)

/** Успешный ответ сервера на зафиксированный вызов outbox. */
internal fun serverMessage(call: OutboxApiCall): MessageDto = MessageDto(
    seq = 1_000L,
    dialogId = call.dialogId,
    senderId = null,
    senderRole = SenderRole.CLIENT.wire,
    type = call.type.wire,
    text = call.text,
    eventType = null,
    payload = null,
    createdAt = "2026-01-01T00:00:00Z",
    file = if (call.bytes != null) {
        FileDto(
            id = "server-${call.clientMessageId}",
            mime = call.mime ?: "application/octet-stream",
            size = call.bytes.size.toLong(),
            durationSec = call.durationSec,
            status = FileStatus.ACTIVE.wire,
        )
    } else {
        null
    },
    clientMessageId = call.clientMessageId,
)
