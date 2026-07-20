package com.al4apps.chatshop.features.chats.domain.model

/**
 * Вложение сообщения. Ссылки на скачивание здесь нет — presigned url временный
 * и не хранится; файл кэшируется локально по стабильному [id] (CLIENT_GUIDE §8).
 */
data class AttachedFile(
    val id: String,
    val mime: String,
    val size: Long,
    val durationSec: Int?,
    val status: FileStatus,
    /** Путь в локальном кэше; null — ещё не скачан. */
    val localPath: String?,
)
