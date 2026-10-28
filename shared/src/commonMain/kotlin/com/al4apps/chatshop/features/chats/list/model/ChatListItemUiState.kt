package com.al4apps.chatshop.features.chats.list.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.ui.UiText

@Immutable
data class ChatListItemUiState(
    val id: String,
    val title: UiText,
    val subtitle: UiText?,
    val status: UiText,
    val statusTone: StatusBadgeTone,
    /** Отображаемая сумма текущего заказа; null — заказа/суммы нет. */
    val amount: String?,
    /** Готовая метка времени (`14:05` / `19.07`); числовой формат, не текст интерфейса. */
    val timestamp: String,
)
