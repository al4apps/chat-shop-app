package com.al4apps.chatshop

import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.ChatsApi
import com.al4apps.chatshop.features.chats.data.DefaultChatsInteractor
import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.OrderCommandRequest
import com.al4apps.chatshop.features.chats.data.dto.SendMessageRequest
import com.al4apps.chatshop.features.chats.domain.DialogsRepository
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

class DefaultChatsInteractorTest {

    @Test
    fun confirmOrderReceivedPersistsServerSnapshot() = runBlocking {
        val api = FakeChatsApi(commandResult = AppResult.Success(completedDialog()))
        val repository = FakeDialogsRepository()
        val interactor = DefaultChatsInteractor(api, repository)

        val result = interactor.confirmOrderReceived("dialog-1")

        assertTrue(result is AppResult.Success)
        assertEquals("ORDER_COMPLETED", api.commandRequest?.type)
        assertEquals("dialog-1", api.commandDialogId)
        assertEquals(OrderStatus.COMPLETED, repository.upserted.single().orderStatus)
    }

    @Test
    fun confirmOrderReceivedPropagatesConflictWithoutWriting() = runBlocking {
        val api = FakeChatsApi(
            commandResult = AppResult.Failure(
                AppError.Api(status = 409, title = "Conflict", detail = "Already completed"),
            ),
        )
        val repository = FakeDialogsRepository()
        val interactor = DefaultChatsInteractor(api, repository)

        val result = interactor.confirmOrderReceived("dialog-1")

        assertTrue(result is AppResult.Failure)
        assertTrue(repository.upserted.isEmpty())
    }

    private fun completedDialog() = DialogDto(
        id = "dialog-1",
        status = "ACTIVE",
        orderStatus = "COMPLETED",
        orderAmount = "1250.00",
        orderDescription = "Тормозные колодки",
        orderNo = 101,
        createdAt = "2026-01-01T00:00:00Z",
        hasOperator = true,
    )

    private class FakeChatsApi(
        private val commandResult: AppResult<DialogDto>,
    ) : ChatsApi {
        var commandDialogId: String? = null
        var commandRequest: OrderCommandRequest? = null

        override suspend fun createDialog(): AppResult<DialogDto> = unsupported()
        override suspend fun sendTextMessage(
            dialogId: String,
            request: SendMessageRequest,
        ): AppResult<MessageDto> = unsupported()

        override suspend fun sendImage(
            dialogId: String,
            bytes: ByteArray,
            mime: String,
            fileName: String,
            clientMessageId: String,
        ): AppResult<MessageDto> = unsupported()

        override suspend fun sendVoice(
            dialogId: String,
            bytes: ByteArray,
            mime: String,
            fileName: String,
            durationSec: Int,
            clientMessageId: String,
        ): AppResult<MessageDto> = unsupported()

        override suspend fun sendOrderCommand(
            dialogId: String,
            request: OrderCommandRequest,
        ): AppResult<DialogDto> {
            commandDialogId = dialogId
            commandRequest = request
            return commandResult
        }

        private fun unsupported(): AppResult<Nothing> = AppResult.Failure(AppError.Unknown)
    }

    private class FakeDialogsRepository : DialogsRepository {
        var upserted: List<Dialog> = emptyList()

        override fun observeDialogs(): Flow<List<Dialog>> = flowOf(emptyList())

        override fun observeDialog(id: String): Flow<Dialog?> = flowOf(null)

        override suspend fun hasDialogs(): Boolean = false

        override suspend fun upsertFromServer(dialogs: List<Dialog>) {
            upserted = dialogs
        }

        override suspend fun lastSyncedSeq(dialogId: String): Long = 0

        override suspend fun advanceLastSyncedSeq(dialogId: String, seq: Long) = Unit
    }
}
