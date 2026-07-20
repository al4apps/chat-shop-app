package com.al4apps.chatshop.features.auth.code.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.design.model.ButtonUiState
import com.al4apps.chatshop.core.ui.UiText

@Immutable
data class CodeUiState(
    val subtitle: UiText,
    val codeDigits: String,
    val isLoading: Boolean,
    val error: UiText?,
    val resendButton: ButtonUiState,
)
