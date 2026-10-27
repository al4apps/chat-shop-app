package com.al4apps.chatshop

import com.al4apps.chatshop.features.chats.data.test.TestChatsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalUuidApi::class)
class TestChatsStoreTest {

    @Test
    fun messagesStayInTheirDialogWithGlobalSeq() = runBlocking {
        val store = TestChatsStore()
        store.reset()

        val dialogs = store.dialogs()
        val first = dialogs.first { it.orderNo == 101 }
        val second = dialogs.first { it.orderNo == 102 }
        val third = store.createDialog()

        store.addClientText(first.id, "first-1", "client-first-1")
        store.addClientText(second.id, "second-1", "client-second-1")
        store.addClientText(third.id, "third-1", "client-third-1")
        store.addClientText(first.id, "first-2", "client-first-2")

        val firstPage = store.messages(first.id, afterSeq = 0, limit = 100).items
        val secondPage = store.messages(second.id, afterSeq = 0, limit = 100).items
        val thirdPage = store.messages(third.id, afterSeq = 0, limit = 100).items

        assertEquals(
            listOf("first-1", "first-2"),
            firstPage.filter { it.clientMessageId != null }.map { it.text },
        )
        assertEquals(
            listOf("second-1"),
            secondPage.filter { it.clientMessageId != null }.map { it.text },
        )
        assertEquals(
            listOf("third-1"),
            thirdPage.filter { it.clientMessageId != null }.map { it.text },
        )

        assertTrue(firstPage.all { it.dialogId == first.id })
        assertTrue(secondPage.all { it.dialogId == second.id })
        assertTrue(thirdPage.all { it.dialogId == third.id })
        assertTrue(firstPage.filter { it.clientMessageId != null }.all { it.senderRole == "CLIENT" })

        val allSeq = (firstPage + secondPage + thirdPage).map { it.seq }
        assertEquals(allSeq.size, allSeq.toSet().size)
        val newSeq = (firstPage + secondPage + thirdPage)
            .filter { it.clientMessageId != null }
            .map { it.seq }
        assertTrue(newSeq.all { it > 6 })
    }

    @Test
    fun clientMessageIdIsIdempotentForTextAndFiles() = runBlocking {
        val store = TestChatsStore()
        store.reset()
        val dialog = store.dialogs().first()

        val firstText = store.addClientText(dialog.id, "hello", "same-client-id")
        val repeatedText = store.addClientText(dialog.id, "hello again", "same-client-id")

        assertEquals(firstText, repeatedText)
        assertEquals(
            1,
            store.messages(dialog.id, afterSeq = 0, limit = 100).items
                .count { it.clientMessageId == "same-client-id" },
        )

        val firstFile = store.addClientFile(
            dialogId = dialog.id,
            type = "IMAGE",
            mime = "image/jpeg",
            size = 10,
            durationSec = null,
            clientMessageId = "same-file-client-id",
        )
        val repeatedFile = store.addClientFile(
            dialogId = dialog.id,
            type = "IMAGE",
            mime = "image/jpeg",
            size = 10,
            durationSec = null,
            clientMessageId = "same-file-client-id",
        )

        assertEquals(firstFile, repeatedFile)
        assertEquals(
            1,
            store.messages(dialog.id, afterSeq = 0, limit = 100).items
                .count { it.clientMessageId == "same-file-client-id" },
        )
        assertEquals(firstFile.file?.id, repeatedFile.file?.id)
    }

    @Test
    fun repeatedClientTextDoesNotEmitAutoReplySignalAgain() = runBlocking {
        val store = TestChatsStore()
        store.reset()
        val dialog = store.dialogs().first()
        val emittedDialogIds = mutableListOf<String>()
        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            store.clientMessages.collect { emittedDialogIds += it }
        }

        store.addClientText(dialog.id, "first", "same-client-id")
        store.addClientText(dialog.id, "second", "same-client-id")
        delay(50)
        collector.cancelAndJoin()

        assertEquals(listOf(dialog.id), emittedDialogIds)
    }

    @Test
    fun testIdentifiersAreUuid() = runBlocking {
        val store = TestChatsStore()
        store.reset()

        val seededDialog = store.dialogs().first()
        assertNotNull(Uuid.parse(seededDialog.id))

        val createdDialog = store.createDialog()
        assertNotNull(Uuid.parse(createdDialog.id))

        val fileMessage = store.addClientFile(
            dialogId = createdDialog.id,
            type = "IMAGE",
            mime = "image/jpeg",
            size = 10,
            durationSec = null,
            clientMessageId = "file-uuid",
        )
        assertNotNull(Uuid.parse(fileMessage.file!!.id))
        Unit
    }
}
