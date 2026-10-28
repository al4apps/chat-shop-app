package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.session.SessionManager
import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

/** Собранный outbox с фейками и минимальной задержкой ретраев. */
internal class OutboxTestHarness(
    initial: List<Message>,
    val api: FakeOutboxChatsApi = FakeOutboxChatsApi(),
    val repository: FakeMessagesRepository = FakeMessagesRepository(initial),
    val filesRepository: FakeFilesRepository = FakeFilesRepository(),
    val fileStore: FakeOutboxFileStore = FakeOutboxFileStore(),
    retryPolicy: RetryPolicy = RetryPolicy(backoffMs = 1),
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessionManager = SessionManager(FakeTokenStorage()).apply { onAuthorized() }

    @Suppress("unused")
    private val service = OutboxService(
        chatsApi = api,
        messagesRepository = repository,
        filesRepository = filesRepository,
        fileStore = fileStore,
        sessionManager = sessionManager,
        scope = scope,
        retryPolicy = retryPolicy,
    )

    /** Ждёт условие, не полагаясь на фиксированный sleep. */
    suspend fun await(condition: () -> Boolean) {
        withTimeout(5_000L) {
            while (!condition()) {
                delay(1L)
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}

/** Запускает сценарий и гарантированно останавливает корутины сервиса. */
internal suspend fun withOutboxHarness(
    initial: List<Message>,
    api: FakeOutboxChatsApi = FakeOutboxChatsApi(),
    filesRepository: FakeFilesRepository = FakeFilesRepository(),
    fileStore: FakeOutboxFileStore = FakeOutboxFileStore(),
    retryPolicy: RetryPolicy = RetryPolicy(backoffMs = 1),
    block: suspend (OutboxTestHarness) -> Unit,
) {
    val harness = OutboxTestHarness(
        initial = initial,
        api = api,
        filesRepository = filesRepository,
        fileStore = fileStore,
        retryPolicy = retryPolicy,
    )
    try {
        block(harness)
    } finally {
        harness.close()
    }
}
