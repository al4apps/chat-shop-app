package com.al4apps.chatshop.features.chats.dialog.model

import kotlinx.serialization.Serializable
import com.al4apps.chatshop.core.ui.UiText

/** Логическое состояние экрана диалога (лента и проекция — из БД, не сохраняются). */
@Serializable
data class DialogVmState(
    val inputText: String = "",
    /** Путь изображения, открытого на весь экран; null — просмотр закрыт. */
    val fullscreenImagePath: String? = null,
    /** Команда ORDER_COMPLETED выполняется. */
    val isConfirmingReceipt: Boolean = false,
    /** Текст ошибки команды; null — ошибки нет. */
    val commandError: UiText? = null,
    /** Сервер вернул 403: действие скрыто до переоткрытия экрана. */
    val isConfirmActionForbidden: Boolean = false,
)
