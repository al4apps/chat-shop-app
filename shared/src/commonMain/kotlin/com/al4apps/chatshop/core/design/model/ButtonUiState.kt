package com.al4apps.chatshop.core.design.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.ui.UiText
import kotlinx.serialization.Serializable

/** Состояние кнопки, собираемое в Assembler и передаваемое в виджеты кнопок как есть. */
@Serializable
@Immutable
data class ButtonUiState(
    val text: UiText,
    val enabled: Boolean,
)
