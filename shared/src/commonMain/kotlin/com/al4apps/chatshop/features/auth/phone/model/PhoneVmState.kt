package com.al4apps.chatshop.features.auth.phone.model

import com.al4apps.chatshop.core.ui.UiText
import kotlinx.serialization.Serializable

/** Логическое состояние шага ввода телефона. */
@Serializable
data class PhoneVmState(
    /** 10 цифр номера после +7. */
    val phoneDigits: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
) {

    companion object {
        /** Цифр в номере после кода страны +992 (Таджикистан). */
        const val PHONE_LENGTH = 9
    }
}
