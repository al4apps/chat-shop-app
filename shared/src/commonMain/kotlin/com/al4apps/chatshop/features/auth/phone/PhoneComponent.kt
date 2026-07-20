package com.al4apps.chatshop.features.auth.phone

import com.al4apps.chatshop.features.auth.phone.model.PhoneUiState
import kotlinx.coroutines.flow.StateFlow

/** Шаг ввода номера телефона. */
interface PhoneComponent {

    val uiState: StateFlow<PhoneUiState>

    fun onPhoneChange(input: String)

    fun onContinueClick()
}
