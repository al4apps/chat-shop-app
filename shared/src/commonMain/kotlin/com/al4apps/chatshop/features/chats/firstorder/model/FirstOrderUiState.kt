package com.al4apps.chatshop.features.chats.firstorder.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.ui.UiText

@Immutable
data class FirstOrderUiState(
    val inputText: String,
    val isSendEnabled: Boolean,
    /** Идёт создание диалога и отправка — блокирует повторные действия. */
    val isSubmitting: Boolean,
    val error: UiText?,
    /** Доступна ли запись голосовых на платформе. */
    val canRecord: Boolean,
    val isRecording: Boolean,
    val recordingSeconds: Int,
)
