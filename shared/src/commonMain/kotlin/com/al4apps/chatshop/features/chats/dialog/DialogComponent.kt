package com.al4apps.chatshop.features.chats.dialog

import com.al4apps.chatshop.features.chats.dialog.model.DialogUiState
import kotlinx.coroutines.flow.StateFlow

/** Экран диалога: лента из БД, отправка текста через outbox. */
interface DialogComponent {

    val uiState: StateFlow<DialogUiState>

    fun onInputChange(text: String)

    fun onSendClick()

    fun onRetryClick(localId: Long)

    fun onBackClick()
}
