package com.al4apps.chatshop.services.outbox

/**
 * Минимальный порт файлового хранилища для outbox.
 *
 * Сервису нужны только чтение локальных байтов и сохранение серверного файла
 * под его id. Отдельный интерфейс позволяет тестировать отправку без Android
 * Context и реального диска; production-реализация — [FileStoreOutboxAdapter].
 */
interface OutboxFileStore {

    /**
     * Читает локальные байты файла по [id].
     *
     * @return байты или null, если файл отсутствует.
     */
    suspend fun readBytes(id: String): ByteArray?

    /**
     * Сохраняет [bytes] под [id].
     *
     * @return абсолютный путь сохранённого файла.
     */
    suspend fun save(id: String, bytes: ByteArray): String
}
