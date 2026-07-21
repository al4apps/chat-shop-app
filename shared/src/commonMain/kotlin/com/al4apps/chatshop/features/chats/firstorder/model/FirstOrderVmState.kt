package com.al4apps.chatshop.features.chats.firstorder.model

import com.al4apps.chatshop.core.ui.UiText
import kotlinx.serialization.Serializable

/** Логическое состояние экрана первого заказа. */
@Serializable
data class FirstOrderVmState(
    val inputText: String = "",
    val isSubmitting: Boolean = false,
    val error: UiText? = null,
)
