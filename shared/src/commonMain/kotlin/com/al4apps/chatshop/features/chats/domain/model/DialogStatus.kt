package com.al4apps.chatshop.features.chats.domain.model

/** Жизненный цикл диалога (CLIENT_GUIDE §11). UNKNOWN — неизвестное серверное значение. */
enum class DialogStatus(val wire: String) {
    NEW("NEW"),
    ASSIGNED("ASSIGNED"),
    ACTIVE("ACTIVE"),
    CLOSED("CLOSED"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String?): DialogStatus =
            entries.firstOrNull { it.wire == value } ?: UNKNOWN
    }
}
