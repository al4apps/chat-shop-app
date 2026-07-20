package com.al4apps.chatshop.services.sync

import com.al4apps.chatshop.core.util.Money
import com.al4apps.chatshop.features.chats.domain.model.AttachedFile
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.FileStatus
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import com.al4apps.chatshop.services.sync.dto.DialogDto
import com.al4apps.chatshop.services.sync.dto.FileDto
import com.al4apps.chatshop.services.sync.dto.MessageDto
import kotlinx.datetime.Instant

/** Битые данные (невалидное время) пропускаются, не роняя синхронизацию. */
fun DialogDto.toDomainOrNull(): Dialog? {
    val created = parseInstantOrNull(createdAt) ?: return null
    return Dialog(
        id = id,
        status = DialogStatus.fromWire(status),
        orderStatus = OrderStatus.fromWireOrNull(orderStatus),
        orderAmount = orderAmount?.let(Money::parseOrNull),
        orderDescription = orderDescription,
        orderNo = orderNo,
        lastMessageAt = lastMessageAt?.let(::parseInstantOrNull),
        lastMessageSeq = lastMessageSeq,
        createdAt = created,
        hasOperator = hasOperator,
    )
}

fun MessageDto.toDomainOrNull(): Message? {
    val created = parseInstantOrNull(createdAt) ?: return null
    return Message(
        localId = 0L, // назначается БД; при upsert с сервера не используется
        seq = seq,
        dialogId = dialogId,
        clientMessageId = clientMessageId,
        senderRole = SenderRole.fromWire(senderRole),
        type = MessageType.fromWire(type),
        text = text,
        eventType = eventType,
        payload = payload?.toString(),
        createdAt = created,
        sendState = SendState.SYNCED,
        file = file?.toDomain(),
    )
}

fun FileDto.toDomain(): AttachedFile = AttachedFile(
    id = id,
    mime = mime,
    size = size,
    durationSec = durationSec,
    status = FileStatus.fromWire(status),
    localPath = null,
)

private fun parseInstantOrNull(value: String): Instant? = try {
    Instant.parse(value)
} catch (e: IllegalArgumentException) {
    null
}
