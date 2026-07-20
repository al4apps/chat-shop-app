package com.al4apps.chatshop.features.chats.data.dto

import kotlinx.serialization.Serializable

/** Диалог с сервера (CLIENT_GUIDE §4). Время — ISO-8601 строки. */
@Serializable
data class DialogDto(
    val id: String,
    val status: String,
    val orderStatus: String? = null,
    val orderAmount: String? = null,
    val orderDescription: String? = null,
    val orderNo: Int = 0,
    val lastMessageAt: String? = null,
    val lastMessageSeq: Long? = null,
    val createdAt: String,
    val hasOperator: Boolean = false,
)
