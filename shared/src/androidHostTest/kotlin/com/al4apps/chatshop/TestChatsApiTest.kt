package com.al4apps.chatshop

import com.al4apps.chatshop.core.network.AppError
import com.al4apps.chatshop.core.network.AppResult
import com.al4apps.chatshop.features.chats.data.dto.OrderCommandRequest
import com.al4apps.chatshop.features.chats.data.test.TestChatsApi
import com.al4apps.chatshop.features.chats.data.test.TestChatsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class TestChatsApiTest {

    @Test
    fun orderCompletedIsAllowedOnlyForProcessing() = runBlocking {
        val store = TestChatsStore().apply { reset() }
        val api = TestChatsApi(store)
        val processing = store.dialogs().first { it.orderStatus == "PROCESSING" }
        val paid = store.dialogs().first { it.orderStatus == "PAID" }

        assertTrue(api.sendOrderCommand(processing.id, OrderCommandRequest("ORDER_COMPLETED")) is AppResult.Success)

        val conflict = api.sendOrderCommand(paid.id, OrderCommandRequest("ORDER_COMPLETED"))
        assertTrue(conflict is AppResult.Failure)
        assertEquals(409, (conflict.error as AppError.Api).status)
    }

    @Test
    fun operatorCommandIsForbiddenForClient() = runBlocking {
        val store = TestChatsStore().apply { reset() }
        val api = TestChatsApi(store)
        val processing = store.dialogs().first { it.orderStatus == "PROCESSING" }

        val forbidden = api.sendOrderCommand(processing.id, OrderCommandRequest("ORDER_PAID"))

        assertTrue(forbidden is AppResult.Failure)
        assertEquals(403, (forbidden.error as AppError.Api).status)
    }
}
