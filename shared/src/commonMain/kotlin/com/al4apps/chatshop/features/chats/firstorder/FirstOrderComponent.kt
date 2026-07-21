package com.al4apps.chatshop.features.chats.firstorder

import com.al4apps.chatshop.features.chats.firstorder.model.FirstOrderUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Особый экран первого заказа (открывается вместо списка, пока диалогов нет).
 * Первая отправка создаёт диалог и переходит в обычный диалог.
 */
interface FirstOrderComponent {

    val uiState: StateFlow<FirstOrderUiState>

    /** Есть ли возврат назад (экран открыт поверх списка через «+»), иначе это корень. */
    val hasBack: Boolean

    fun onInputChange(text: String)

    fun onSendClick()

    fun onBackClick()

    /** Кнопка аудиозаписи — активируется на этапе 10 (аудио). */
    fun onRecordClick()
}
