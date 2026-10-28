package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.features.chats.domain.model.MessageType

/** Зафиксированный сетевой вызов outbox — то, что реально ушло в `ChatsApi`. */
data class OutboxApiCall(
    val dialogId: String,
    val clientMessageId: String,
    val type: MessageType,
    val text: String? = null,
    val bytes: ByteArray? = null,
    val mime: String? = null,
    val durationSec: Int? = null,
)
