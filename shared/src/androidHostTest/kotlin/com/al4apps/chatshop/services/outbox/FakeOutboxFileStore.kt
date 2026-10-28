package com.al4apps.chatshop.services.outbox

/** In-memory файловое хранилище: без Android Context и реального диска. */
class FakeOutboxFileStore : OutboxFileStore {

    val files: MutableMap<String, ByteArray> = mutableMapOf()
    val savedIds: MutableList<String> = mutableListOf()

    override suspend fun readBytes(id: String): ByteArray? = files[id]

    override suspend fun save(id: String, bytes: ByteArray): String {
        files[id] = bytes.copyOf()
        savedIds += id
        return "/cache/$id"
    }
}
