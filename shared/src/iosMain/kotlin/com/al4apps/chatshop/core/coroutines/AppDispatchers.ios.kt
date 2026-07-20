package com.al4apps.chatshop.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual object AppDispatchers {

    actual val io: CoroutineDispatcher = Dispatchers.IO
}
