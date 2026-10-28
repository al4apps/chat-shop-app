package com.al4apps.chatshop

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.order_number
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.media.AudioPlaybackState
import com.al4apps.chatshop.core.media.VoiceRecordingState
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.util.Money
import com.al4apps.chatshop.features.chats.dialog.DialogAssembler
import com.al4apps.chatshop.features.chats.dialog.model.DialogVmState
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.Message
import com.al4apps.chatshop.features.chats.domain.model.MessageType
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import com.al4apps.chatshop.features.chats.domain.model.SendState
import com.al4apps.chatshop.features.chats.domain.model.SenderRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.datetime.Instant

class DialogAssemblerTest {

    @Test
    fun processingEventIsParsedAndOnlyItShowsConfirmAction() {
        val ui = assemble(
            dialog = dialog(OrderStatus.PROCESSING),
            messages = listOf(
                system(id = 1, eventType = "ORDER_PAID", payload = """{"orderNo":101,"amount":"1250.00"}"""),
                system(id = 2, eventType = "ORDER_PROCESSING", payload = """{"orderNo":101,"amount":"1250.00"}"""),
            ),
        )

        assertEquals(UiText(Res.string.order_number, "101"), ui.title)
        assertEquals("1 250,00", ui.orderSummary?.amount)
        assertEquals(StatusBadgeTone.ACCENT, ui.orderSummary?.tone)
        val processing = ui.items.first { it.eventType == "ORDER_PROCESSING" }
        assertEquals("1 250,00", processing.orderEvent?.amount)
        assertTrue(processing.showConfirmAction)
        assertFalse(ui.items.first { it.eventType == "ORDER_PAID" }.showConfirmAction)
    }

    @Test
    fun confirmActionIsHiddenWhenForbiddenOrOrderIsTerminal() {
        val forbidden = assemble(
            dialog = dialog(OrderStatus.PROCESSING),
            messages = listOf(
                system(id = 1, eventType = "ORDER_PROCESSING", payload = """{"orderNo":101,"amount":"1250.00"}"""),
            ),
            vmState = DialogVmState(isConfirmActionForbidden = true),
        )
        assertFalse(forbidden.items.single().showConfirmAction)

        val completed = assemble(
            dialog = dialog(OrderStatus.COMPLETED),
            messages = listOf(
                system(id = 1, eventType = "ORDER_PROCESSING", payload = """{"orderNo":101,"amount":"1250.00"}"""),
            ),
        )
        assertFalse(completed.items.single().showConfirmAction)
    }

    @Test
    fun invalidPayloadDoesNotCrash() {
        val ui = assemble(
            dialog = dialog(OrderStatus.PROCESSING),
            messages = listOf(
                system(id = 1, eventType = "ORDER_PROCESSING", payload = "{not-json"),
            ),
        )

        assertNotNull(ui.items.single().orderEvent)
        assertEquals(null, ui.items.single().orderEvent?.amount)
    }

    private fun assemble(
        dialog: Dialog,
        messages: List<Message>,
        vmState: DialogVmState = DialogVmState(),
    ) = DialogAssembler().assemble(
        vmState = vmState,
        dialog = dialog,
        messages = messages,
        recording = VoiceRecordingState(),
        canRecord = false,
        playback = AudioPlaybackState(),
    )

    private fun dialog(orderStatus: OrderStatus) = Dialog(
        id = "dialog-1",
        status = DialogStatus.ACTIVE,
        orderStatus = orderStatus,
        orderAmount = Money(125_000),
        orderDescription = "Тормозные колодки",
        orderNo = 101,
        lastMessageAt = Instant.fromEpochMilliseconds(1_000),
        lastMessageSeq = 2,
        createdAt = Instant.fromEpochMilliseconds(0),
        hasOperator = true,
    )

    private fun system(
        id: Long,
        eventType: String,
        payload: String,
    ) = Message(
        localId = id,
        seq = id,
        dialogId = "dialog-1",
        clientMessageId = null,
        senderRole = SenderRole.SYSTEM,
        type = MessageType.SYSTEM,
        text = eventType,
        eventType = eventType,
        payload = payload,
        createdAt = Instant.fromEpochMilliseconds(id),
        sendState = SendState.SYNCED,
        file = null,
    )
}
