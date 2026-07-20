package com.al4apps.chatshop.features.chats.domain.model

import kotlinx.datetime.Instant

/** Элемент ленты диалога. */
data class Message(
    /** Стабильный локальный ключ (в т.ч. у исходящих до подтверждения сервером). */
    val localId: Long,
    /** Серверный курсор; null у ещё не подтверждённых исходящих. */
    val seq: Long?,
    val dialogId: String,
    /** Идемпотентный ключ исходящего (CLIENT_GUIDE §10); null у входящих/SYSTEM. */
    val clientMessageId: String?,
    val senderRole: SenderRole,
    val type: MessageType,
    val text: String?,
    /** Тип SYSTEM-события; логика UI строится на нём, не на тексте (CLIENT_GUIDE §6). */
    val eventType: String?,
    /** Сырой json payload SYSTEM-события; разбирается на этапе статусов. */
    val payload: String?,
    val createdAt: Instant,
    val sendState: SendState,
    val file: AttachedFile?,
)
