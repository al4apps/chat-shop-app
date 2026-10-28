package com.al4apps.chatshop.features.chats.domain.model

/** Команды заказа, доступные клиенту. Остальные команды — операторские. */
enum class OrderCommand(val wire: String) {
    ORDER_COMPLETED("ORDER_COMPLETED"),
}
