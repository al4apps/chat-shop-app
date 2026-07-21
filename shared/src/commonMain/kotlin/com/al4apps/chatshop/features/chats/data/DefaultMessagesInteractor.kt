package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalUuidApi::class)
class DefaultMessagesInteractor(
    private val messagesRepository: MessagesRepository,
) : MessagesInteractor {

    override fun observeMessages(dialogId: String): Flow<List<Message>> =
        messagesRepository.observeMessages(dialogId)

    override suspend fun sendText(dialogId: String, text: String) {
        messagesRepository.enqueueText(
            dialogId = dialogId,
            clientMessageId = Uuid.random().toString(),
            text = text,
        )
    }

    override suspend fun retryMessage(localId: Long) {
        messagesRepository.retry(localId)
    }
}
