package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.SendState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory замена репозитория: повторяет переходы PENDING → SENDING → SYNCED/FAILED.
 * Хранит историю подтверждений и ошибок, чтобы тест проверял адресацию по `localId`.
 */
class FakeMessagesRepository(
    initial: List<Message> = emptyList(),
) : MessagesRepository {

    private val state = MutableStateFlow(initial.sortedBy { it.localId })

    val messages: StateFlow<List<Message>> = state.asStateFlow()
    val confirmed: MutableList<Pair<Long, Message>> = mutableListOf()
    val failed: MutableList<Long> = mutableListOf()
    val sending: MutableList<Long> = mutableListOf()
    var resetStuckSendingCalls: Int = 0
        private set

    fun pendingMessages(): List<Message> =
        state.value.filter { it.sendState == SendState.PENDING }.sortedBy { it.localId }

    override fun observeMessages(dialogId: String): Flow<List<Message>> =
        state.map { messages -> messages.filter { it.dialogId == dialogId } }

    override suspend fun upsertFromServer(messages: List<Message>) {
        state.update { current ->
            current.map { local ->
                messages.firstOrNull { it.clientMessageId == local.clientMessageId } ?: local
            }
        }
    }

    override suspend fun enqueueText(
        dialogId: String,
        clientMessageId: String,
        text: String,
    ) = Unit

    override suspend fun enqueueImage(
        dialogId: String,
        clientMessageId: String,
        fileId: String,
        mime: String,
        size: Long,
        localPath: String,
    ) = Unit

    override suspend fun enqueueVoice(
        dialogId: String,
        clientMessageId: String,
        fileId: String,
        mime: String,
        size: Long,
        durationSec: Int,
        localPath: String,
    ) = Unit

    override fun observePending(): Flow<List<Message>> =
        state.map { messages ->
            messages.filter { it.sendState == SendState.PENDING }.sortedBy { it.localId }
        }

    override suspend fun markSending(localId: Long) {
        sending += localId
        update(localId) { it.copy(sendState = SendState.SENDING) }
    }

    override suspend fun markFailed(localId: Long) {
        failed += localId
        update(localId) { it.copy(sendState = SendState.FAILED) }
    }

    override suspend fun retry(localId: Long) {
        update(localId) {
            if (it.sendState == SendState.FAILED) it.copy(sendState = SendState.PENDING) else it
        }
    }

    override suspend fun resetStuckSending() {
        resetStuckSendingCalls++
        state.update { messages ->
            messages.map {
                if (it.sendState == SendState.SENDING) it.copy(sendState = SendState.PENDING) else it
            }
        }
    }

    override suspend fun confirmSent(localId: Long, serverMessage: Message) {
        confirmed += localId to serverMessage
        update(localId) {
            it.copy(
                seq = serverMessage.seq,
                senderRole = serverMessage.senderRole,
                type = serverMessage.type,
                text = serverMessage.text,
                eventType = serverMessage.eventType,
                payload = serverMessage.payload,
                createdAt = serverMessage.createdAt,
                sendState = SendState.SYNCED,
                file = serverMessage.file,
                clientMessageId = serverMessage.clientMessageId ?: it.clientMessageId,
            )
        }
    }

    private fun update(localId: Long, transform: (Message) -> Message) {
        state.update { messages ->
            messages.map { if (it.localId == localId) transform(it) else it }
        }
    }
}
