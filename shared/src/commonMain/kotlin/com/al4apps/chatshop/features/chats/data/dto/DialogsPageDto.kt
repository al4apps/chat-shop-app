package com.al4apps.chatshop.features.chats.data.dto

import kotlinx.serialization.Serializable

/** Ответ `GET /dialogs`. */
@Serializable
data class DialogsPageDto(
    val items: List<DialogDto> = emptyList(),
)
