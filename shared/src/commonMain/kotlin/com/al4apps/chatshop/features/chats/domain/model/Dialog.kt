package com.al4apps.chatshop.features.chats.domain.model

import com.al4apps.chatshop.core.util.Money
import kotlinx.datetime.Instant

/** Диалог с проекцией текущего заказа (CLIENT_GUIDE §4, §7). */
data class Dialog(
    val id: String,
    val status: DialogStatus,
    val orderStatus: OrderStatus?,
    val orderAmount: Money?,
    val orderDescription: String?,
    /** Порядковый номер заказа в диалоге; 0 — заказов ещё не было. */
    val orderNo: Int,
    val lastMessageAt: Instant?,
    val lastMessageSeq: Long?,
    val createdAt: Instant,
    val hasOperator: Boolean,
)
