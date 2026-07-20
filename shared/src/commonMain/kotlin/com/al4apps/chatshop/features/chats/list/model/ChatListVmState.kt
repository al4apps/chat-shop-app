package com.al4apps.chatshop.features.chats.list.model

import com.al4apps.chatshop.core.ui.UiText
import kotlinx.serialization.Serializable

/** Логическое состояние списка чатов (сами диалоги — из БД, не сохраняются). */
@Serializable
data class ChatListVmState(
    val isCreating: Boolean = false,
    val error: UiText? = null,
)
