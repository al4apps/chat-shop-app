package com.al4apps.chatshop.features.chats.list

import chatshop.shared.generated.resources.Res
import chatshop.shared.generated.resources.order_number
import com.al4apps.chatshop.core.ui.UiText
import com.al4apps.chatshop.core.util.toListTimestamp
import com.al4apps.chatshop.features.chats.domain.model.Dialog
import com.al4apps.chatshop.features.chats.list.model.ChatListItemUiState
import com.al4apps.chatshop.features.chats.list.model.ChatListUiState
import com.al4apps.chatshop.features.chats.ui.toBadgeTone
import com.al4apps.chatshop.features.chats.ui.toUiText
import kotlinx.collections.immutable.toImmutableList

/** Собирает uiState списка из диалогов БД. */
class ChatListAssembler {

    fun assemble(dialogs: List<Dialog>): ChatListUiState {
        // Fallback для старых/тестовых данных без серверного orderNo: номер по createdAt,
        // чтобы он оставался стабильным при сортировке по последней активности.
        val fallbackNumberById = dialogs
            .sortedBy { it.createdAt }
            .withIndex()
            .associate { (index, dialog) -> dialog.id to (index + 1) }

        return ChatListUiState(
            items = dialogs
                .map { it.toItem(fallbackNumberById[it.id] ?: 0) }
                .toImmutableList(),
            isEmpty = dialogs.isEmpty(),
        )
    }

    private fun Dialog.toItem(fallbackOrderNumber: Int): ChatListItemUiState {
        val displayOrderNo = orderNo.takeIf { it > 0 } ?: fallbackOrderNumber
        return ChatListItemUiState(
            id = id,
            title = UiText(Res.string.order_number, displayOrderNo.toString()),
            subtitle = orderDescription
                ?.takeIf { it.isNotBlank() }
                ?.let { UiText(it) },
            status = orderStatus?.toUiText() ?: status.toUiText(),
            statusTone = orderStatus?.toBadgeTone() ?: status.toBadgeTone(),
            amount = orderAmount
                ?.takeIf { orderStatus != null }
                ?.asDisplayString(),
            timestamp = (lastMessageAt ?: createdAt).toListTimestamp(),
        )
    }
}
