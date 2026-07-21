package com.al4apps.chatshop.features.chats.data.dto

import kotlinx.serialization.Serializable

/** Тело `POST /dialogs/{id}/messages` — только TEXT (CLIENT_GUIDE §6, §10). */
@Serializable
data class SendMessageRequest(
    val text: String,
    val clientMessageId: String,
)
