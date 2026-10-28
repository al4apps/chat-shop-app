package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.media.FileStore

/** Адаптер production-хранилища [FileStore] к узкому порту outbox. */
class FileStoreOutboxAdapter(
    private val fileStore: FileStore,
) : OutboxFileStore {

    override suspend fun readBytes(id: String): ByteArray? = fileStore.readBytes(id)

    override suspend fun save(id: String, bytes: ByteArray): String = fileStore.save(id, bytes)
}
