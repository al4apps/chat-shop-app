package com.al4apps.chatshop.features.chats.data.dto

import kotlinx.serialization.Serializable

/** Ответ `GET /dialogs/{id}/messages` (CLIENT_GUIDE §5). */
@Serializable
data class MessagesPageDto(
    val items: List<MessageDto> = emptyList(),
    val nextAfterSeq: Long? = null,
)
