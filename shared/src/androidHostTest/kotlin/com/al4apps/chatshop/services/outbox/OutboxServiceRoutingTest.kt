package com.al4apps.chatshop.services.outbox

import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class OutboxServiceRoutingTest {

    @Test
    fun routesEachTextToItsDialogAndConfirmsOwnLocalRow() = runBlocking {
        withOutboxHarness(
            initial = listOf(
                outgoingText(localId = 1, dialogId = DIALOG_A),
                outgoingText(localId = 2, dialogId = DIALOG_B),
            ),
        ) { harness ->
            harness.await { harness.repository.confirmed.size == 2 }

            assertEquals(listOf(DIALOG_A, DIALOG_B), harness.api.calls.map { it.dialogId })
            assertEquals(listOf(1L, 2L), harness.repository.confirmed.map { it.first })
            assertEquals(
                DIALOG_A,
                harness.repository.confirmed.first { it.first == 1L }.second.dialogId,
            )
            assertEquals(
                DIALOG_B,
                harness.repository.confirmed.first { it.first == 2L }.second.dialogId,
            )
            assertEquals(
                SenderRole.CLIENT,
                harness.repository.confirmed.first { it.first == 1L }.second.senderRole,
            )
            assertEquals(
                "client-1",
                harness.repository.confirmed.first { it.first == 1L }.second.clientMessageId,
            )
            assertTrue(harness.repository.failed.isEmpty())
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }

    @Test
    fun keepsOrderOfMessagesInsideDialog() = runBlocking {
        withOutboxHarness(
            initial = listOf(
                outgoingText(localId = 1, dialogId = DIALOG_A, text = "first"),
                outgoingText(localId = 2, dialogId = DIALOG_A, text = "second"),
            ),
        ) { harness ->
            harness.await { harness.repository.confirmed.size == 2 }

            assertEquals(listOf("client-1", "client-2"), harness.api.calls.map { it.clientMessageId })
            assertEquals(listOf("first", "second"), harness.api.calls.map { it.text })
            assertEquals(listOf(1L, 2L), harness.repository.confirmed.map { it.first })
        }
    }

    @Test
    fun retriesWithSameClientMessageIdAndConfirmsOnce() = runBlocking {
        var attempts = 0
        val api = FakeOutboxChatsApi { call ->
            attempts++
            if (attempts < 3) {
                AppResult.Failure(AppError.Network)
            } else {
                AppResult.Success(serverMessage(call))
            }
        }

        withOutboxHarness(
            initial = listOf(outgoingText(localId = 1, dialogId = DIALOG_A)),
            api = api,
        ) { harness ->
            harness.await { harness.repository.confirmed.size == 1 }

            assertEquals(3, api.calls.size)
            assertEquals(
                listOf("client-1", "client-1", "client-1"),
                api.calls.map { it.clientMessageId },
            )
            assertEquals(1, harness.repository.confirmed.size)
            assertTrue(harness.repository.failed.isEmpty())
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }

    @Test
    fun manualRetryOfFailedMessageKeepsClientMessageId() = runBlocking {
        var calls = 0
        val api = FakeOutboxChatsApi { call ->
            calls++
            if (calls == 1) {
                AppResult.Failure(
                    AppError.Api(status = 500, title = "Error", detail = "Temporary"),
                )
            } else {
                AppResult.Success(serverMessage(call))
            }
        }

        withOutboxHarness(
            initial = listOf(outgoingText(localId = 1, dialogId = DIALOG_A)),
            api = api,
        ) { harness ->
            harness.await { harness.repository.failed.contains(1L) }

            harness.repository.retry(1L)
            harness.await { harness.repository.confirmed.size == 1 }

            assertEquals(listOf("client-1", "client-1"), api.calls.map { it.clientMessageId })
            assertEquals(1, harness.repository.confirmed.size)
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }

    @Test
    fun serverErrorFailsOnlyItsDialogAndContinuesWithNext() = runBlocking {
        val api = FakeOutboxChatsApi { call ->
            if (call.dialogId == DIALOG_A) {
                AppResult.Failure(
                    AppError.Api(status = 409, title = "Conflict", detail = "Dialog closed"),
                )
            } else {
                AppResult.Success(serverMessage(call))
            }
        }

        withOutboxHarness(
            initial = listOf(
                outgoingText(localId = 1, dialogId = DIALOG_A),
                outgoingText(localId = 2, dialogId = DIALOG_B),
            ),
            api = api,
        ) { harness ->
            harness.await {
                harness.repository.failed.contains(1L) &&
                    harness.repository.confirmed.any { it.first == 2L }
            }

            assertEquals(listOf(1L), harness.repository.failed)
            assertEquals(listOf(2L), harness.repository.confirmed.map { it.first })
            assertEquals(listOf(DIALOG_A, DIALOG_B), api.calls.map { it.dialogId })
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }

    @Test
    fun networkExhaustionFailsFirstDialogAndStillSendsSecond() = runBlocking {
        val api = FakeOutboxChatsApi { call ->
            if (call.dialogId == DIALOG_A) {
                AppResult.Failure(AppError.Network)
            } else {
                AppResult.Success(serverMessage(call))
            }
        }

        withOutboxHarness(
            initial = listOf(
                outgoingText(localId = 1, dialogId = DIALOG_A),
                outgoingText(localId = 2, dialogId = DIALOG_B),
            ),
            api = api,
        ) { harness ->
            harness.await {
                harness.repository.failed.contains(1L) &&
                    harness.repository.confirmed.any { it.first == 2L }
            }

            assertEquals(
                listOf(DIALOG_A, DIALOG_A, DIALOG_A, DIALOG_B),
                api.calls.map { it.dialogId },
            )
            assertEquals(3, api.calls.count { it.dialogId == DIALOG_A })
            assertEquals(listOf(1L), harness.repository.failed)
            assertEquals(listOf(2L), harness.repository.confirmed.map { it.first })
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }

    @Test
    fun resetsStuckSendingAndSendsItOnStart() = runBlocking {
        val stuck = outgoingText(localId = 1, dialogId = DIALOG_A)
            .copy(sendState = SendState.SENDING)

        withOutboxHarness(initial = listOf(stuck)) { harness ->
            harness.await { harness.repository.confirmed.size == 1 }

            assertEquals(1, harness.repository.resetStuckSendingCalls)
            assertEquals(listOf(1L), harness.repository.confirmed.map { it.first })
            assertEquals(listOf("client-1"), harness.api.calls.map { it.clientMessageId })
            assertEquals(listOf(1L), harness.repository.sending)
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }

    @Test
    fun associatesLocalImageBytesWithServerFileId() = runBlocking {
        val bytes = byteArrayOf(1, 2, 3)
        val image = outgoingImage(
            localId = 1,
            dialogId = DIALOG_A,
            fileId = "local-file",
            mime = "image/jpeg",
            size = bytes.size.toLong(),
        )

        val fileStore = FakeOutboxFileStore().apply {
            files["local-file"] = bytes
        }

        withOutboxHarness(initial = listOf(image), fileStore = fileStore) { harness ->
            harness.await { harness.repository.confirmed.size == 1 }

            val call = harness.api.calls.single()
            assertEquals("image/jpeg", call.mime)
            assertTrue(call.bytes?.contentEquals(bytes) == true)
            assertEquals(listOf("server-client-1"), harness.fileStore.savedIds)
            assertEquals(
                "/cache/server-client-1",
                harness.filesRepository.localPaths["server-client-1"],
            )
            assertEquals(
                "server-client-1",
                harness.repository.confirmed.single().second.file?.id,
            )
        }
    }

    @Test
    fun failsWithoutNetworkCallWhenClientMessageIdIsMissing() = runBlocking {
        val withoutId = outgoingText(localId = 1, dialogId = DIALOG_A)
            .copy(clientMessageId = null)

        withOutboxHarness(initial = listOf(withoutId)) { harness ->
            harness.await { harness.repository.failed.contains(1L) }

            assertTrue(harness.api.calls.isEmpty())
            assertTrue(harness.repository.confirmed.isEmpty())
            assertTrue(harness.repository.pendingMessages().isEmpty())
        }
    }
}
