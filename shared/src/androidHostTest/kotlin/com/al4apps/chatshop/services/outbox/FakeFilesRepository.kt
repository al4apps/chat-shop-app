package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.features.chats.domain.DownloadableFile
import com.al4apps.chatshop.features.chats.domain.FilesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Файловый репозиторий для проверки привязки серверного file.id к локальному кэшу. */
class FakeFilesRepository : FilesRepository {

    val localPaths: MutableMap<String, String> = mutableMapOf()

    override suspend fun setLocalPath(fileId: String, localPath: String) {
        localPaths[fileId] = localPath
    }

    override suspend fun localPath(fileId: String): String? = localPaths[fileId]

    override fun observeDownloadable(): Flow<List<DownloadableFile>> = flowOf(emptyList())
}
