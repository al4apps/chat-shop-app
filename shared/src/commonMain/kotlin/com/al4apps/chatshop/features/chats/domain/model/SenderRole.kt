package com.al4apps.chatshop.features.chats.domain.model

/** Роль отправителя сообщения. */
enum class SenderRole(val wire: String) {
    CLIENT("CLIENT"),
    OPERATOR("OPERATOR"),
    ADMIN("ADMIN"),
    SYSTEM("SYSTEM"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String?): SenderRole =
            entries.firstOrNull { it.wire == value } ?: UNKNOWN
    }
}
