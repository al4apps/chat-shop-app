package com.al4apps.chatshop.services.testmode

import com.al4apps.chatshop.features.chats.data.test.TestChatsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Тестовый сервис авто-ответов оператора: на каждое исходящее текстовое сообщение
 * клиента через паузу добавляет в стор ответ оператора — следующий поллинг подтянет
 * его в ленту. Создаётся только в тестовом DI-модуле (работает лишь в тест-режиме).
 */
class TestAutoReplyService(
    private val store: TestChatsStore,
    private val scope: CoroutineScope,
) {

    init {
        scope.launch {
            store.clientMessages.collect { dialogId ->
                delay(AUTO_REPLY_DELAY_MS)
                store.addOperatorText(dialogId, REPLIES.random())
            }
        }
    }

    private companion object {
        const val AUTO_REPLY_DELAY_MS = 1_500L

        val REPLIES = listOf(
            "Принял, уточняю наличие и вернусь с ответом.",
            "Да, всё в силе. Ожидайте подтверждения.",
            "Спасибо! Заказ обрабатывается.",
            "Уточнил: доставка займёт около часа.",
        )
    }
}
