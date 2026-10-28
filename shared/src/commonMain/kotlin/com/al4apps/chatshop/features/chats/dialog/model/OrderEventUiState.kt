package com.al4apps.chatshop.features.chats.dialog.model

import androidx.compose.runtime.Immutable

/** Разобранный payload SYSTEM-события заказа; text остаётся fallback-ом. */
@Immutable
data class OrderEventUiState(
    val eventType: String,
    val orderNo: Int?,
    val amount: String?,
    val description: String?,
)
