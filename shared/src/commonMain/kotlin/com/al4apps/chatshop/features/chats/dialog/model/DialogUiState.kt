package com.al4apps.chatshop.features.chats.dialog.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.ui.UiText
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class DialogUiState(
    val title: UiText,
    /** Проекция текущего заказа для шапки; null — заказа нет. */
    val orderSummary: OrderSummaryUiState?,
    /** Элементы ленты, новые первыми (под reverseLayout списка). */
    val items: ImmutableList<MessageItemUiState>,
    val inputText: String,
    val isSendEnabled: Boolean,
    /** Диалог закрыт — ввод недоступен. */
    val isInputBlocked: Boolean,
    /** Команда подтверждения получения выполняется. */
    val isConfirmingReceipt: Boolean,
    /** Ошибка команды подтверждения; null — ошибки нет. */
    val commandError: UiText?,
    /** Открытое на весь экран изображение; null — просмотр закрыт. */
    val fullscreenImagePath: String?,
    /** Доступна ли запись голосовых на платформе. */
    val canRecord: Boolean,
    val isRecording: Boolean,
    val recordingSeconds: Int,
)
