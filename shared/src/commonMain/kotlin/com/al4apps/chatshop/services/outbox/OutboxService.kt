package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.SessionState
import com.al4apps.chatshop.features.chats.data.ChatsApi
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import com.al4apps.chatshop.features.chats.data.toDomainOrNull
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Доставка исходящих: подписан на PENDING-записи БД, шлёт по одному
 * последовательно (порядок в рамках диалога сохраняется).
 *
 * - Успех: серверное сообщение транзакционно заменяет локальную строку
 *   по `clientMessageId` (получает seq, sendState=SYNCED).
 * - Сетевые ошибки: ретраи с backoff, затем FAILED (ручной повтор из UI —
 *   тем же `clientMessageId`, сервер вернёт уже созданное без дубля).
 * - Ошибки сервера (4xx/409): FAILED сразу.
 *
 * Активен при авторизованной сессии независимо от foreground —
 * доставляем, пока жив процесс. Ничего не поллит (это делает SyncService).
 */
class OutboxService(
    private val chatsApi: ChatsApi,
    private val messagesRepository: MessagesRepository,
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
        messagesRepository.markSending(message.localId)

        var attempt = 0
        while (true) {
            val result = chatsApi.sendTextMessage(
                dialogId = message.dialogId,
                request = SendMessageRequest(
                    text = message.text.orEmpty(),
                    clientMessageId = clientMessageId,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    val serverMessage = result.value.toDomainOrNull()
                    if (serverMessage != null) {
                        // Сверка по localId — не зависит от эха clientMessageId сервером.
                        messagesRepository.confirmSent(message.localId, serverMessage)
                    } else {
                        messagesRepository.markFailed(message.localId)
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

    private companion object {
        const val MAX_NETWORK_ATTEMPTS = 3
        const val RETRY_BACKOFF_MS = 2_000L
    }
}
