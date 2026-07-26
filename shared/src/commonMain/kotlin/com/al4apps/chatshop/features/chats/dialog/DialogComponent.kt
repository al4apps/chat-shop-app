package com.al4apps.chatshop.features.chats.dialog

import com.al4apps.chatshop.features.chats.dialog.model.DialogUiState
import kotlinx.coroutines.flow.StateFlow

/** Экран диалога: лента из БД, отправка текста через outbox. */
interface DialogComponent {

    val uiState: StateFlow<DialogUiState>

    fun onInputChange(text: String)

    fun onSendClick()

    /** Прикрепить изображение: системный пикер → отправка через outbox. */
    fun onAttachClick()

    /** Начать запись голосового. */
    fun onRecordStart()

    /** Остановить запись и отправить. */
    fun onRecordStop()

    /** Отменить запись без отправки. */
    fun onRecordCancel()

    /** Открыть изображение на весь экран. */
    fun onImageClick(imagePath: String)

    /** Закрыть полноэкранный просмотр. */
    fun onFullscreenDismiss()

    fun onRetryClick(localId: Long)

    fun onBackClick()
}
