package com.al4apps.chatshop.features.chats.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.chats.domain.DownloadableFile
import com.al4apps.chatshop.features.chats.domain.FilesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultFilesRepository(
    private val db: ChatShopDatabase,
    private val ioDispatcher: CoroutineDispatcher = AppDispatchers.io,
) : FilesRepository {

    override suspend fun setLocalPath(fileId: String, localPath: String) {
        withContext(ioDispatcher) {
            db.filesQueries.updateLocalPath(localPath = localPath, id = fileId)
        }
    }

    override suspend fun localPath(fileId: String): String? = withContext(ioDispatcher) {
        db.filesQueries.selectById(fileId).executeAsOneOrNull()?.localPath
    }

    override fun observeDownloadable(): Flow<List<DownloadableFile>> =
        db.filesQueries.selectDownloadable()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows ->
                rows.mapNotNull { row ->
                    row.remoteUrl?.let { DownloadableFile(id = row.id, remoteUrl = it) }
                }
            }
}
