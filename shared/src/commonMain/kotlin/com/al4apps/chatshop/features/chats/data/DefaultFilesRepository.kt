package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.coroutines.AppDispatchers
import com.al4apps.chatshop.db.ChatShopDatabase
import com.al4apps.chatshop.features.chats.domain.FilesRepository
import kotlinx.coroutines.CoroutineDispatcher
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
}
