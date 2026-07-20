package com.al4apps.chatshop.features.chats.domain.model

/** Статус текущего заказа диалога (проекция, CLIENT_GUIDE §7). */
enum class OrderStatus(val wire: String) {
    CREATED("CREATED"),
    PAID("PAID"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWireOrNull(value: String?): OrderStatus? = when (value) {
            null -> null
            else -> entries.firstOrNull { it.wire == value } ?: UNKNOWN
        }
    }
}
