package com.al4apps.chatshop.features.chats.dialog.model

import kotlinx.serialization.Serializable

/** Логическое состояние экрана диалога (лента и проекция — из БД, не сохраняются). */
@Serializable
data class DialogVmState(
    val inputText: String = "",
)
