package com.al4apps.chatshop.features.auth.code.model

import com.al4apps.chatshop.core.ui.UiText
import kotlinx.serialization.Serializable

/** Логическое состояние шага ввода SMS-кода. */
@Serializable
data class CodeVmState(
    /** Полный номер, на который отправлен код: `+7XXXXXXXXXX`. */
    val phone: String,
    val codeDigits: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    /** Секунд до возможности повторной отправки кода. */
    val resendSecondsLeft: Int = RESEND_COOLDOWN_SECONDS,
) {

    companion object {
        const val CODE_LENGTH = 6
        const val RESEND_COOLDOWN_SECONDS = 60
    }
}
