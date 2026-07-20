package com.al4apps.chatshop.features.auth.phone.model

import androidx.compose.runtime.Immutable
import com.al4apps.chatshop.core.design.model.ButtonUiState
import com.al4apps.chatshop.core.ui.UiText

@Immutable
data class PhoneUiState(
    val phoneDigits: String,
    val isLoading: Boolean,
    val error: UiText?,
    val continueButton: ButtonUiState,
)
