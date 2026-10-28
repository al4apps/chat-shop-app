package com.al4apps.chatshop.features.chats.data.dto

import kotlinx.serialization.Serializable

/** Тело `POST /dialogs/{id}/commands` (CLIENT_GUIDE §6). */
@Serializable
data class OrderCommandRequest(
    val type: String,
    val description: String? = null,
    val amount: String? = null,
)
