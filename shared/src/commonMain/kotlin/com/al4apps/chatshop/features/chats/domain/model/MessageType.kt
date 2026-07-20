package com.al4apps.chatshop.features.chats.domain.model

/** Тип элемента ленты. */
enum class MessageType(val wire: String) {
    TEXT("TEXT"),
    IMAGE("IMAGE"),
    VOICE("VOICE"),
    SYSTEM("SYSTEM"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String?): MessageType =
            entries.firstOrNull { it.wire == value } ?: UNKNOWN
    }
}
