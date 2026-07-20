package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.util.Money
import com.al4apps.chatshop.db.Dialogs
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import kotlinx.datetime.Instant

fun Dialogs.toDomain(): Dialog = Dialog(
    id = id,
    status = DialogStatus.fromWire(status),
    orderStatus = OrderStatus.fromWireOrNull(orderStatus),
    orderAmount = orderAmount?.let(Money::parseOrNull),
    orderDescription = orderDescription,
    orderNo = orderNo.toInt(),
    lastMessageAt = lastMessageAt?.let(Instant::fromEpochMilliseconds),
    lastMessageSeq = lastMessageSeq,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    hasOperator = hasOperator != 0L,
)
