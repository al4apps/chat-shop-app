package com.al4apps.chatshop.services.files

import com.al4apps.chatshop.core.media.FileStore
import com.al4apps.chatshop.core.network.isSuccess
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.SessionState
import com.al4apps.chatshop.features.chats.domain.DownloadableFile
import com.al4apps.chatshop.features.chats.domain.FilesRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Скачивание входящих вложений (CLIENT_GUIDE §8): реагирует на файлы без локального кэша,
 * качает по временной presigned-ссылке **отдельным клиентом без Authorization** и кэширует
 * по стабильному `file.id`. Ошибка (протухший url) — не критична: следующий опрос обновит
 * `remoteUrl`, и файл снова попадёт в очередь скачивания.
 */
class FileDownloadService(
    private val plainClient: HttpClient,
    private val filesRepository: FilesRepository,
    private val fileStore: FileStore,
    private val sessionManager: SessionManager,
    private val scope: CoroutineScope,
) {

    init {
        scope.launch {
            sessionManager.state.collectLatest { session ->
                if (session !is SessionState.Authorized) return@collectLatest
                filesRepository.observeDownloadable().collect { files ->
                    files.forEach { download(it) }
                }
            }
        }
    }

    private suspend fun download(file: DownloadableFile) {
        if (fileStore.exists(file.id)) {
            filesRepository.setLocalPath(file.id, fileStore.pathFor(file.id))
            return
        }
        try {
            val response = plainClient.get(file.remoteUrl)
            if (!response.status.isSuccess()) return
            val bytes = response.readRawBytes()
            val path = fileStore.save(file.id, bytes)
            filesRepository.setLocalPath(file.id, path)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Сеть/протухший url — оставляем на следующий опрос.
        }
    }
}
