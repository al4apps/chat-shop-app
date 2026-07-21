package com.al4apps.chatshop.features.chats.list

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.chat_list_order_number
import chatshop.shared.generated.resources.dialog_status_active
import chatshop.shared.generated.resources.dialog_status_assigned
import chatshop.shared.generated.resources.dialog_status_closed
import chatshop.shared.generated.resources.dialog_status_new
import chatshop.shared.generated.resources.order_status_cancelled
import chatshop.shared.generated.resources.order_status_completed
import chatshop.shared.generated.resources.order_status_created
import chatshop.shared.generated.resources.order_status_paid
import chatshop.shared.generated.resources.order_status_processing
import chatshop.shared.generated.resources.order_status_unknown
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.util.toListTimestamp
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.domain.model.DialogStatus
import com.al4apps.chatshop.features.chats.domain.model.OrderStatus
import com.al4apps.chatshop.features.chats.list.model.ChatListItemUiState
import com.al4apps.chatshop.features.chats.list.model.ChatListUiState
import kotlinx.collections.immutable.toImmutableList

/** Собирает uiState списка из диалогов БД. */
class ChatListAssembler {

    fun assemble(dialogs: List<Dialog>): ChatListUiState {
        // Порядковый номер заказа без описания — по времени создания (createdAt неизменен),
        // поэтому номер стабилен независимо от сортировки списка по активности.
        val orderNumberById = dialogs
            .sortedBy { it.createdAt }
            .withIndex()
            .associate { (index, dialog) -> dialog.id to (index + 1) }

        return ChatListUiState(
            items = dialogs
                .map { it.toItem(orderNumberById[it.id] ?: 0) }
                .toImmutableList(),
            isEmpty = dialogs.isEmpty(),
        )
    }

    private fun Dialog.toItem(orderNumber: Int): ChatListItemUiState = ChatListItemUiState(
        id = id,
        title = orderDescription
            ?.takeIf { it.isNotBlank() }
            ?.let { UiText(it) }
            ?: UiText(Res.string.chat_list_order_number, orderNumber.toString()),
        subtitle = orderStatus?.toUiText() ?: status.toUiText(),
        timestamp = (lastMessageAt ?: createdAt).toListTimestamp(),
    )

    private fun DialogStatus.toUiText(): UiText = when (this) {
        DialogStatus.NEW -> UiText(Res.string.dialog_status_new)
        DialogStatus.ASSIGNED -> UiText(Res.string.dialog_status_assigned)
        DialogStatus.ACTIVE -> UiText(Res.string.dialog_status_active)
        DialogStatus.CLOSED -> UiText(Res.string.dialog_status_closed)
        DialogStatus.UNKNOWN -> UiText(Res.string.order_status_unknown)
    }

    private fun OrderStatus.toUiText(): UiText = when (this) {
        OrderStatus.CREATED -> UiText(Res.string.order_status_created)
        OrderStatus.PAID -> UiText(Res.string.order_status_paid)
        OrderStatus.PROCESSING -> UiText(Res.string.order_status_processing)
        OrderStatus.COMPLETED -> UiText(Res.string.order_status_completed)
        OrderStatus.CANCELLED -> UiText(Res.string.order_status_cancelled)
        OrderStatus.UNKNOWN -> UiText(Res.string.order_status_unknown)
    }
}
