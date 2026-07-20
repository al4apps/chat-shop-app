package com.al4apps.chatshop.features.chats.list

import com.al4apps.chatshop.features.chats.list.model.ChatListUiState
import kotlinx.coroutines.flow.StateFlow

/** Список чатов: диалоги из БД, создание нового диалога. */
interface ChatListComponent {

    val uiState: StateFlow<ChatListUiState>

    fun onDialogClick(dialogId: String)

    fun onNewDialogClick()
}
