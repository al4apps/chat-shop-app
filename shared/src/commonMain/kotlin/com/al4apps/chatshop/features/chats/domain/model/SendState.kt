package com.al4apps.chatshop.features.chats.domain.model

/** Локальное состояние доставки сообщения (outbox-паттерн). */
enum class SendState {
    /** Подтверждено сервером (есть seq). */
    SYNCED,

    /** В очереди на отправку. */
    PENDING,

    /** Отправляется прямо сейчас. */
    SENDING,

    /** Отправка не удалась, доступен ручной повтор. */
    FAILED,
    ;

    companion object {
        fun fromDb(value: String): SendState =
            entries.firstOrNull { it.name == value } ?: SYNCED
    }
}
