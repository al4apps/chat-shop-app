package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.db.SelectByDialog
import com.al4apps.chatshop.db.SelectPending
import com.al4apps.chatshop.features.chats.domain.model.AttachedFile
import com.al4apps.chatshop.features.chats.domain.model.FileStatus
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import kotlinx.datetime.Instant

fun SelectByDialog.toDomain(): Message = Message(
    localId = localId,
    seq = seq,
    dialogId = dialogId,
    clientMessageId = clientMessageId,
    senderRole = SenderRole.fromWire(senderRole),
    type = MessageType.fromWire(type),
    text = text,
    eventType = eventType,
    payload = payload,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    sendState = SendState.fromDb(sendState),
    file = toAttachedFile(),
)

fun SelectPending.toDomain(): Message = Message(
    localId = localId,
    seq = seq,
    dialogId = dialogId,
    clientMessageId = clientMessageId,
    senderRole = SenderRole.fromWire(senderRole),
    type = MessageType.fromWire(type),
    text = text,
    eventType = eventType,
    payload = payload,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    sendState = SendState.fromDb(sendState),
    file = fileId?.let { id ->
        AttachedFile(
            id = id,
            mime = mime.orEmpty(),
            size = size ?: 0L,
            durationSec = durationSec?.toInt(),
            status = FileStatus.fromWire(fileStatus),
            localPath = localPath,
        )
    },
)

private fun SelectByDialog.toAttachedFile(): AttachedFile? {
    val fileId = fileId ?: return null
    return AttachedFile(
        id = fileId,
        mime = mime.orEmpty(),
        size = size ?: 0L,
        durationSec = durationSec?.toInt(),
        status = FileStatus.fromWire(fileStatus),
        localPath = localPath,
    )
}
