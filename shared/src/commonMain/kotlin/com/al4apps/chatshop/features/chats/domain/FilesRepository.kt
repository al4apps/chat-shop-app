package com.al4apps.chatshop.features.chats.domain

import kotlinx.coroutines.flow.Flow

/** Файл, готовый к скачиванию: активен, ещё нет локального кэша, есть свежий url. */
data class DownloadableFile(
    val id: String,
    val remoteUrl: String,
)

/** Метаданные и локальные пути вложений. */
interface FilesRepository {

    /** Привязывает локальный путь кэша к файлу (после скачивания или отправки). */
    suspend fun setLocalPath(fileId: String, localPath: String)

    /** Локальный путь файла в кэше, если он уже сохранён. */
    suspend fun localPath(fileId: String): String?

    /** Поток файлов, которые нужно скачать (для FileDownloadService). */
    fun observeDownloadable(): Flow<List<DownloadableFile>>
}
