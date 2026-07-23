package com.al4apps.chatshop.features.chats.data

import com.al4apps.chatshop.core.media.FileStore
import com.al4apps.chatshop.core.media.IMAGE_MAX_BYTES
import com.al4apps.chatshop.core.media.PickedImage
import com.al4apps.chatshop.core.media.compressImageToJpeg
import com.al4apps.chatshop.features.chats.domain.MessagesInteractor
import com.al4apps.chatshop.features.chats.domain.MessagesRepository
import com.al4apps.chatshop.features.chats.domain.model.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalUuidApi::class)
class DefaultMessagesInteractor(
    private val messagesRepository: MessagesRepository,
    private val fileStore: FileStore,
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

    override suspend fun sendImage(dialogId: String, image: PickedImage) {
        val jpeg = compressImageToJpeg(image.bytes, IMAGE_MAX_BYTES)
        val fileId = Uuid.random().toString()
        val localPath = fileStore.save(fileId, jpeg)
        messagesRepository.enqueueImage(
            dialogId = dialogId,
            clientMessageId = Uuid.random().toString(),
            fileId = fileId,
            mime = "image/jpeg",
            size = jpeg.size.toLong(),
            localPath = localPath,
        )
    }

    override suspend fun retryMessage(localId: Long) {
        messagesRepository.retry(localId)
    }
}
