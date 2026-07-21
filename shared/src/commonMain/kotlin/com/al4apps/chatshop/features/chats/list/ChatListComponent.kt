package com.al4apps.chatshop.features.chats.list

import com.al4apps.chatshop.features.chats.list.model.ChatListUiState
import kotlinx.coroutines.flow.StateFlow

/** Список заказов: диалоги из БД. Создание нового заказа — на экране нового заказа. */
interface ChatListComponent {

    val uiState: StateFlow<ChatListUiState>

    fun onDialogClick(dialogId: String)

    fun onNewDialogClick()
}
