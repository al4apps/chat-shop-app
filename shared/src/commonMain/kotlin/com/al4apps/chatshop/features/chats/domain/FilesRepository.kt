package com.al4apps.chatshop.features.chats.domain

/** Метаданные и локальные пути вложений. */
interface FilesRepository {

    /** Привязывает локальный путь кэша к файлу (после скачивания или отправки). */
    suspend fun setLocalPath(fileId: String, localPath: String)

    /** Локальный путь файла в кэше, если он уже сохранён. */
    suspend fun localPath(fileId: String): String?
}
