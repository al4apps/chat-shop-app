package com.al4apps.chatshop.features.home

import kotlinx.coroutines.flow.StateFlow

/**
 * Контейнер всего после авторизации. Пока заглушка: показывает счётчик диалогов
 * из БД (сквозная проверка sync → БД → UI); список чатов — этап 6.
 */
interface HomeComponent {

    val dialogsCount: StateFlow<Int>
}
