package com.al4apps.chatshop.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/** Платформенные диспетчеры (common Dispatchers.IO недоступен публично). */
expect object AppDispatchers {

    /** Диспетчер блокирующего IO (БД, файлы). */
    val io: CoroutineDispatcher
}
