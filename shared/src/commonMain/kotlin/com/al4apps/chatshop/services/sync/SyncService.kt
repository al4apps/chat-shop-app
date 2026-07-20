package com.al4apps.chatshop.services.sync

import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.core.session.SessionState
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

/**
 * Приём данных: инкрементальный поллинг сервера → кэширование в БД (CLIENT_GUIDE §5).
 * Только приём — отправкой занимается OutboxService (этап 7).
 *
 * Ритм:
 * - список диалогов — каждые [DIALOGS_POLL_MS], пока приложение на переднем плане;
 * - открытый диалог — каждые [MESSAGES_POLL_MS] по курсору `afterSeq`;
 * - в фоне и без авторизации поллинг остановлен; возврат в foreground — немедленный pull.
 *
 * Ошибки сети не прерывают циклы — следующий тик повторит запрос, UI живёт из БД.
 */
class SyncService(
    private val syncApi: SyncApi,
    private val dialogsRepository: DialogsRepository,
    private val messagesRepository: MessagesRepository,
    private val sessionManager: SessionManager,
    private val scope: CoroutineScope,
) {

    private val isForeground = MutableStateFlow(false)
    private val activeDialogId = MutableStateFlow<String?>(null)

    init {
        launchDialogsLoop()
        launchMessagesLoop()
    }

    fun onAppForeground() {
        isForeground.value = true
    }

    fun onAppBackground() {
        isForeground.value = false
    }

    /** Диалог открыт на экране — включает частый поллинг его ленты. */
    fun onDialogOpened(dialogId: String) {
        activeDialogId.value = dialogId
    }

    fun onDialogClosed() {
        activeDialogId.value = null
    }

    /** Разовая синхронизация списка диалогов (например, для Bootstrap-роутинга Home). */
    suspend fun syncDialogsOnce() {
        val result = syncApi.getDialogs()
        if (result is AppResult.Success) {
            dialogsRepository.upsertFromServer(
                result.value.items.mapNotNull { it.toDomainOrNull() },
            )
        }
    }

    /** Разовая догрузка ленты диалога по курсору (все доступные страницы). */
    suspend fun syncMessagesOnce(dialogId: String) {
        var afterSeq = dialogsRepository.lastSyncedSeq(dialogId)
        while (true) {
            val result = syncApi.getMessages(dialogId = dialogId, afterSeq = afterSeq)
            if (result !is AppResult.Success) return
            val page = result.value
            val messages = page.items.mapNotNull { it.toDomainOrNull() }
            if (messages.isEmpty()) return

            messagesRepository.upsertFromServer(messages)
            val newCursor = page.nextAfterSeq ?: messages.maxOf { it.seq ?: 0L }
            dialogsRepository.advanceLastSyncedSeq(dialogId, newCursor)
            afterSeq = newCursor

            if (page.items.size < SyncApi.MESSAGES_PAGE_LIMIT) return
        }
    }

    private fun launchDialogsLoop() {
        scope.launch {
            combine(sessionManager.state, isForeground) { session, foreground ->
                session is SessionState.Authorized && foreground
            }
                .distinctUntilChanged()
                .collectLatest { active ->
                    if (!active) return@collectLatest
                    while (isActive) {
                        syncDialogsOnce()
                        delay(DIALOGS_POLL_MS)
                    }
                }
        }
    }

    private fun launchMessagesLoop() {
        scope.launch {
            combine(
                sessionManager.state,
                isForeground,
                activeDialogId,
            ) { session, foreground, dialogId ->
                dialogId.takeIf { session is SessionState.Authorized && foreground }
            }
                .distinctUntilChanged()
                .collectLatest { dialogId ->
                    if (dialogId == null) return@collectLatest
                    while (isActive) {
                        syncMessagesOnce(dialogId)
                        delay(MESSAGES_POLL_MS)
                    }
                }
        }
    }

    private companion object {
        const val DIALOGS_POLL_MS = 12_000L
        const val MESSAGES_POLL_MS = 4_000L
    }
}
