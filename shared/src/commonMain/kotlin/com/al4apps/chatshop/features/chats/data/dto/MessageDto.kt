package com.al4apps.chatshop.features.chats.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Элемент ленты с сервера (CLIENT_GUIDE §4). */
@Serializable
data class MessageDto(
    val seq: Long,
    val dialogId: String,
    val senderId: String? = null,
    val senderRole: String,
    val type: String,
    val text: String? = null,
    val eventType: String? = null,
    /** Структурированные данные SYSTEM-события; храним сырым json-текстом. */
    val payload: JsonElement? = null,
    val createdAt: String,
    val file: FileDto? = null,
    /** Идемпотентный ключ нашего исходящего, если сервер его возвращает. */
    val clientMessageId: String? = null,
)
