package com.al4apps.chatshop.features.auth.code

import com.al4apps.chatshop.features.auth.code.model.CodeUiState
import kotlinx.coroutines.flow.StateFlow

/** Шаг ввода SMS-кода. Код отправляется автоматически при вводе всех цифр. */
interface CodeComponent {

    val uiState: StateFlow<CodeUiState>

    fun onCodeChange(input: String)

    fun onResendClick()

    fun onBackClick()
}
