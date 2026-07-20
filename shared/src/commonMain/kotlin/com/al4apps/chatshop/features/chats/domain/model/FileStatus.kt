package com.al4apps.chatshop.features.chats.domain.model

/** Состояние файла на сервере: DELETED — удалён по TTL, показывается заглушка. */
enum class FileStatus(val wire: String) {
    ACTIVE("ACTIVE"),
    DELETED("DELETED"),
    ;

    companion object {
        fun fromWire(value: String?): FileStatus =
            entries.firstOrNull { it.wire == value } ?: ACTIVE
    }
}
