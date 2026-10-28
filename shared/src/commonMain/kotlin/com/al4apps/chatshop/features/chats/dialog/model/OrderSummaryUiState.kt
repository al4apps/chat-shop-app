package com.al4apps.chatshop.features.chats.dialog.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.ui.UiText

/** Проекция текущего заказа для шапки диалога. */
@Immutable
data class OrderSummaryUiState(
    val description: String?,
    val amount: String?,
    val status: UiText,
    val tone: StatusBadgeTone,
)
