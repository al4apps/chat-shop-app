package com.al4apps.chatshop.core.media

/**
 * Локальное хранилище байтов файлов по стабильному id (кэш вложений, CLIENT_GUIDE §8).
 * Создаётся на краю приложения (нужен путь песочницы платформы).
 */
expect class FileStore {

    /** Сохраняет байты под [id], возвращает абсолютный путь. */
    suspend fun save(id: String, bytes: ByteArray): String

    /** Читает байты по [id] или null, если файла нет. */
    suspend fun readBytes(id: String): ByteArray?

    /** Абсолютный путь файла по [id] (может ещё не существовать). */
    fun pathFor(id: String): String

    /** Есть ли файл в кэше. */
    suspend fun exists(id: String): Boolean
}
