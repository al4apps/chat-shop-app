package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.media.FileStore
import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.SessionState
import com.al4apps.chatshop.features.chats.data.ChatsApi
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import com.al4apps.chatshop.features.chats.data.toDomainOrNull
import com.al4apps.chatshop.features.chats.domain.FilesRepository
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Доставка исходящих: подписан на PENDING-записи БД, шлёт по одному
 * последовательно (порядок в рамках диалога сохраняется). Поддерживает TEXT и IMAGE.
 *
 * - Успех: серверное сообщение транзакционно заменяет локальную строку по `localId`
 *   (получает seq, sendState=SYNCED). Для IMAGE локальные байты привязываются к серверному
 *   `file.id`, чтобы своё же изображение не скачивалось повторно.
 * - Сетевые ошибки: ретраи с backoff, затем FAILED (ручной повтор из UI — тем же
 *   `clientMessageId`, сервер вернёт уже созданное без дубля).
 * - Ошибки сервера (4xx/409): FAILED сразу.
 *
 * Активен при авторизованной сессии независимо от foreground. Ничего не поллит.
 */
class OutboxService(
    private val chatsApi: ChatsApi,
    private val messagesRepository: MessagesRepository,
    private val filesRepository: FilesRepository,
    private val fileStore: FileStore,
    private val sessionManager: SessionManager,
    private val scope: CoroutineScope,
) {

    init {
        scope.launch {
            // Процесс мог умереть посреди отправки — вернуть SENDING в очередь.
            messagesRepository.resetStuckSending()

            sessionManager.state.collectLatest { session ->
                if (session !is SessionState.Authorized) return@collectLatest
                messagesRepository.observePending().collect { pending ->
                    // Обрабатывается голова очереди; изменение её sendState
                    // заставит Flow эмитить снова — очередь идёт по одному.
                    val message = pending.firstOrNull() ?: return@collect
                    deliver(message)
                }
            }
        }
    }

    private suspend fun deliver(message: Message) {
        val clientMessageId = message.clientMessageId
        if (clientMessageId == null) {
            messagesRepository.markFailed(message.localId)
            return
        }

        // Для IMAGE заранее читаем локальные байты (потеря файла → FAILED).
        val imageBytes: ByteArray? = if (message.type == MessageType.IMAGE) {
            val bytes = message.file?.id?.let { fileStore.readBytes(it) }
            if (bytes == null) {
                messagesRepository.markFailed(message.localId)
                return
            }
            bytes
        } else {
            null
        }

        messagesRepository.markSending(message.localId)

        var attempt = 0
        while (true) {
            val result = send(message, clientMessageId, imageBytes)
            when (result) {
                is AppResult.Success -> {
                    val serverMessage = result.value.toDomainOrNull()
                    if (serverMessage == null) {
                        messagesRepository.markFailed(message.localId)
                        return
                    }
                    messagesRepository.confirmSent(message.localId, serverMessage)
                    if (imageBytes != null) {
                        associateLocalImage(serverMessage, imageBytes)
                    }
                    return
                }

                is AppResult.Failure -> when (result.error) {
                    AppError.Network -> {
                        attempt++
                        if (attempt >= MAX_NETWORK_ATTEMPTS) {
                            messagesRepository.markFailed(message.localId)
                            return
                        }
                        delay(RETRY_BACKOFF_MS * attempt)
                    }

                    else -> {
                        messagesRepository.markFailed(message.localId)
                        return
                    }
                }
            }
        }
    }

    private suspend fun send(
        message: Message,
        clientMessageId: String,
        imageBytes: ByteArray?,
    ): AppResult<MessageDto> = if (imageBytes != null) {
        chatsApi.sendImage(
            dialogId = message.dialogId,
            bytes = imageBytes,
            mime = message.file?.mime ?: "image/jpeg",
            fileName = "image.jpg",
            clientMessageId = clientMessageId,
        )
    } else {
        chatsApi.sendTextMessage(
            dialogId = message.dialogId,
            request = SendMessageRequest(
                text = message.text.orEmpty(),
                clientMessageId = clientMessageId,
            ),
        )
    }

    /** Кэширует свои байты под серверным file.id, чтобы не скачивать своё же изображение. */
    private suspend fun associateLocalImage(serverMessage: Message, bytes: ByteArray) {
        val serverFileId = serverMessage.file?.id ?: return
        val path = fileStore.save(serverFileId, bytes)
        filesRepository.setLocalPath(serverFileId, path)
    }

    private companion object {
        const val MAX_NETWORK_ATTEMPTS = 3
        const val RETRY_BACKOFF_MS = 2_000L
    }
}
