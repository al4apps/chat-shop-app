package com.al4apps.chatshop

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.order_number
import com.al4apps.chatshop.core.design.model.StatusBadgeTone
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.util.Money
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import com.al4apps.chatshop.features.chats.list.ChatListAssembler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.Instant

class ChatListAssemblerTest {

    @Test
    fun orderRowContainsNumberDescriptionBadgeAndAmount() {
        val item = ChatListAssembler()
            .assemble(listOf(dialog(orderStatus = OrderStatus.PROCESSING, orderNo = 101)))
            .items
            .single()

        assertEquals(UiText(Res.string.order_number, "101"), item.title)
        assertEquals(UiText("Тормозные колодки"), item.subtitle)
        assertEquals("1 250,00", item.amount)
        assertEquals(StatusBadgeTone.ACCENT, item.statusTone)
    }

    @Test
    fun orderWithoutDescriptionAndNumberFallsBackToCreationOrder() {
        val item = ChatListAssembler()
            .assemble(
                listOf(
                    dialog(
                        status = DialogStatus.NEW,
                        orderStatus = null,
                        orderNo = 0,
                        orderDescription = null,
                    ),
                ),
            )
            .items
            .single()

        assertEquals(UiText(Res.string.order_number, "1"), item.title)
        assertNull(item.subtitle)
        assertNull(item.amount)
        assertEquals(StatusBadgeTone.ACCENT, item.statusTone)
    }

    private fun dialog(
        status: DialogStatus = DialogStatus.ACTIVE,
        orderStatus: OrderStatus? = OrderStatus.PROCESSING,
        orderNo: Int = 101,
        orderDescription: String? = "Тормозные колодки",
    ) = Dialog(
        id = "dialog-1",
        status = status,
        orderStatus = orderStatus,
        orderAmount = Money(125_000),
        orderDescription = orderDescription,
        orderNo = orderNo,
        lastMessageAt = Instant.fromEpochMilliseconds(1_000),
        lastMessageSeq = 1,
        createdAt = Instant.fromEpochMilliseconds(0),
        hasOperator = true,
    )
}
