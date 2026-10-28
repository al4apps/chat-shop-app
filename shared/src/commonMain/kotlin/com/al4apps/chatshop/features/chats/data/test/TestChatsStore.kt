package com.al4apps.chatshop.features.chats.data.test

import com.al4apps.chatshop.features.chats.data.dto.DialogDto
import com.al4apps.chatshop.features.chats.data.dto.FileDto
import com.al4apps.chatshop.features.chats.data.dto.MessageDto
import com.al4apps.chatshop.features.chats.data.dto.MessagesPageDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * In-memory «сервер» тестового режима: диалоги и лента в памяти.
 * Общий для [TestSyncApi] (чтение) и [TestChatsApi] (запись), чтобы `seq` не расходились.
 *
 * Идентификаторы диалогов и файлов — UUID, как в production-контракте.
 * `seq` — единый глобальный курсор ленты, а не идентификатор сущности.
 *
 * Данные живут только в рамках рантайма и не персистятся. Набор — текст + SYSTEM-события.
 */
@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class TestChatsStore {

    private val mutex = Mutex()

    private val dialogs = mutableListOf<DialogDto>()
    private val messages = mutableMapOf<String, MutableList<MessageDto>>()
    private var nextSeq = 0L

    private val mutableClientMessages = MutableSharedFlow<String>(extraBufferCapacity = 16)

    /** Сигнал тестовому сервису авто-ответов: в диалоге появилось исходящее сообщение клиента. */
    val clientMessages: SharedFlow<String> = mutableClientMessages.asSharedFlow()

    /** Сбрасывает стор в исходный мок-набор (вызывается при входе в тест-режим). */
    suspend fun reset() = mutex.withLock {
        dialogs.clear()
        messages.clear()
        nextSeq = 0
        seed()
    }

    suspend fun dialogs(): List<DialogDto> = mutex.withLock {
        ensureSeeded()
        dialogs.sortedByDescending { it.lastMessageAt ?: it.createdAt }
    }

    suspend fun messages(dialogId: String, afterSeq: Long, limit: Int): MessagesPageDto =
        mutex.withLock {
            ensureSeeded()
            val page = messages[dialogId].orEmpty()
                .filter { it.seq > afterSeq }
                .sortedBy { it.seq }
                .take(limit)
            MessagesPageDto(items = page, nextAfterSeq = page.lastOrNull()?.seq)
        }

    suspend fun createDialog(): DialogDto = mutex.withLock {
        ensureSeeded()
        val now = nowIso()
        val dialog = DialogDto(
            id = Uuid.random().toString(),
            status = "NEW",
            createdAt = now,
            lastMessageAt = now,
        )
        dialogs += dialog
        messages[dialog.id] = mutableListOf()
        dialog
    }

    suspend fun addClientText(dialogId: String, text: String, clientMessageId: String): MessageDto {
        var created = false
        val message = mutex.withLock {
            ensureSeeded()
            findByClientMessageIdLocked(clientMessageId)
                ?: appendMessageLocked(
                    dialogId = dialogId,
                    senderRole = "CLIENT",
                    type = "TEXT",
                    text = text,
                    clientMessageId = clientMessageId,
                ).also { created = true }
        }
        if (created) mutableClientMessages.tryEmit(dialogId)
        return message
    }

    suspend fun addOperatorText(dialogId: String, text: String): MessageDto = mutex.withLock {
        ensureSeeded()
        appendMessageLocked(
            dialogId = dialogId,
            senderRole = "OPERATOR",
            type = "TEXT",
            text = text,
        )
    }

    suspend fun addClientFile(
        dialogId: String,
        type: String,
        mime: String,
        size: Long,
        durationSec: Int?,
        clientMessageId: String,
    ): MessageDto = mutex.withLock {
        ensureSeeded()
        findByClientMessageIdLocked(clientMessageId)
            ?: appendMessageLocked(
                dialogId = dialogId,
                senderRole = "CLIENT",
                type = type,
                clientMessageId = clientMessageId,
                file = FileDto(
                    id = Uuid.random().toString(),
                    mime = mime,
                    size = size,
                    durationSec = durationSec,
                    url = null,
                    status = "ACTIVE",
                ),
            )
    }

    /**
     * Тестовая команда клиента `ORDER_COMPLETED`.
     *
     * Разрешена только при `orderStatus = PROCESSING`; добавляет SYSTEM-событие
     * и обновляет проекцию заказа. Иначе возвращает null (тестовая реализация
     * маппит это в `409`).
     */
    suspend fun completeOrder(dialogId: String): DialogDto? = mutex.withLock {
        ensureSeeded()
        val index = dialogs.indexOfFirst { it.id == dialogId }
        if (index < 0) return@withLock null

        val current = dialogs[index]
        if (current.orderStatus != "PROCESSING") return@withLock null

        appendMessageLocked(
            dialogId = dialogId,
            senderRole = "SYSTEM",
            type = "SYSTEM",
            text = "Заказ №${current.orderNo} получен",
            eventType = "ORDER_COMPLETED",
            payload = orderPayload(
                orderNo = current.orderNo,
                amount = current.orderAmount,
                description = current.orderDescription,
            ),
        )
        dialogs[index] = dialogs[index].copy(orderStatus = "COMPLETED")
        dialogs[index]
    }

    private fun findByClientMessageIdLocked(clientMessageId: String): MessageDto? =
        messages.values.asSequence()
            .flatMap { it.asSequence() }
            .firstOrNull { it.clientMessageId == clientMessageId }

    private fun appendMessageLocked(
        dialogId: String,
        senderRole: String,
        type: String,
        text: String? = null,
        eventType: String? = null,
        payload: JsonElement? = null,
        clientMessageId: String? = null,
        file: FileDto? = null,
    ): MessageDto {
        val seq = ++nextSeq
        val createdAt = nowIso()
        val message = MessageDto(
            seq = seq,
            dialogId = dialogId,
            senderRole = senderRole,
            type = type,
            text = text,
            eventType = eventType,
            payload = payload,
            createdAt = createdAt,
            file = file,
            clientMessageId = clientMessageId,
        )
        messages.getOrPut(dialogId) { mutableListOf() } += message
        updateDialogLastMessageLocked(dialogId, seq, createdAt)
        return message
    }

    private fun updateDialogLastMessageLocked(dialogId: String, seq: Long, at: String) {
        val index = dialogs.indexOfFirst { it.id == dialogId }
        if (index >= 0) {
            dialogs[index] = dialogs[index].copy(lastMessageSeq = seq, lastMessageAt = at)
        }
    }

    private fun ensureSeeded() {
        if (dialogs.isEmpty()) seed()
    }

    private fun seed() {
        val now = nowIso()
        val firstId = Uuid.random().toString()
        val secondId = Uuid.random().toString()
        val closedId = Uuid.random().toString()
        val cancelledId = Uuid.random().toString()
        val first = DialogDto(
            id = firstId,
            status = "ACTIVE",
            orderStatus = "PROCESSING",
            orderAmount = "1250.00",
            orderDescription = "Тормозные колодки",
            orderNo = 101,
            lastMessageAt = now,
            lastMessageSeq = 5,
            createdAt = now,
            hasOperator = true,
        )
        val second = DialogDto(
            id = secondId,
            status = "ACTIVE",
            orderStatus = "PAID",
            orderAmount = "480.00",
            orderDescription = "Масляный фильтр",
            orderNo = 102,
            lastMessageAt = now,
            lastMessageSeq = 7,
            createdAt = now,
            hasOperator = true,
        )
        val closed = DialogDto(
            id = closedId,
            status = "CLOSED",
            orderStatus = "COMPLETED",
            orderAmount = "620.00",
            orderDescription = "Воздушный фильтр",
            orderNo = 103,
            lastMessageAt = now,
            lastMessageSeq = 10,
            createdAt = now,
            hasOperator = true,
        )
        val cancelled = DialogDto(
            id = cancelledId,
            status = "ACTIVE",
            orderStatus = "CANCELLED",
            orderAmount = "980.00",
            orderDescription = "Свечи зажигания",
            orderNo = 104,
            lastMessageAt = now,
            lastMessageSeq = 12,
            createdAt = now,
            hasOperator = true,
        )
        dialogs += first
        dialogs += second
        dialogs += closed
        dialogs += cancelled
        messages[first.id] = mutableListOf(
            system(
                dialogId = first.id,
                seq = 1,
                eventType = "ORDER_CREATED",
                text = "Заказ №101 создан на сумму 1250.00",
                orderNo = 101,
                amount = "1250.00",
                description = "Тормозные колодки",
                createdAt = now,
            ),
            MessageDto(
                seq = 2,
                dialogId = first.id,
                senderRole = "CLIENT",
                type = "TEXT",
                text = "Здравствуйте! Нужны передние тормозные колодки.",
                createdAt = now,
            ),
            MessageDto(
                seq = 3,
                dialogId = first.id,
                senderRole = "OPERATOR",
                type = "TEXT",
                text = "Добрый день! Есть в наличии, оформляю заказ.",
                createdAt = now,
            ),
            system(
                dialogId = first.id,
                seq = 4,
                eventType = "ORDER_PAID",
                text = "Заказ №101 оплачен",
                orderNo = 101,
                amount = "1250.00",
                createdAt = now,
            ),
            system(
                dialogId = first.id,
                seq = 5,
                eventType = "ORDER_PROCESSING",
                text = "Заказ №101 собирается",
                orderNo = 101,
                amount = "1250.00",
                createdAt = now,
            ),
        )
        messages[second.id] = mutableListOf(
            system(
                dialogId = second.id,
                seq = 6,
                eventType = "ORDER_CREATED",
                text = "Заказ №102 создан на сумму 480.00",
                orderNo = 102,
                amount = "480.00",
                description = "Масляный фильтр",
                createdAt = now,
            ),
            MessageDto(
                seq = 7,
                dialogId = second.id,
                senderRole = "OPERATOR",
                type = "TEXT",
                text = "Заказ оплачен, передаём в доставку.",
                createdAt = now,
            ),
        )
        messages[closed.id] = mutableListOf(
            system(
                dialogId = closed.id,
                seq = 8,
                eventType = "ORDER_CREATED",
                text = "Заказ №103 создан на сумму 620.00",
                orderNo = 103,
                amount = "620.00",
                description = "Воздушный фильтр",
                createdAt = now,
            ),
            system(
                dialogId = closed.id,
                seq = 9,
                eventType = "ORDER_COMPLETED",
                text = "Заказ №103 получен",
                orderNo = 103,
                amount = "620.00",
                createdAt = now,
            ),
            system(
                dialogId = closed.id,
                seq = 10,
                eventType = "DIALOG_CLOSED",
                text = "Диалог закрыт оператором",
                createdAt = now,
            ),
        )
        messages[cancelled.id] = mutableListOf(
            system(
                dialogId = cancelled.id,
                seq = 11,
                eventType = "ORDER_CREATED",
                text = "Заказ №104 создан на сумму 980.00",
                orderNo = 104,
                amount = "980.00",
                description = "Свечи зажигания",
                createdAt = now,
            ),
            system(
                dialogId = cancelled.id,
                seq = 12,
                eventType = "ORDER_CANCELLED",
                text = "Заказ №104 отменён",
                orderNo = 104,
                amount = "980.00",
                createdAt = now,
            ),
        )
        nextSeq = 12
    }

    private fun system(
        dialogId: String,
        seq: Long,
        eventType: String,
        text: String,
        orderNo: Int? = null,
        amount: String? = null,
        description: String? = null,
        createdAt: String,
    ): MessageDto = MessageDto(
        seq = seq,
        dialogId = dialogId,
        senderRole = "SYSTEM",
        type = "SYSTEM",
        text = text,
        eventType = eventType,
        payload = orderPayload(orderNo, amount, description),
        createdAt = createdAt,
    )

    private fun orderPayload(
        orderNo: Int?,
        amount: String?,
        description: String?,
    ): JsonElement = buildJsonObject {
        if (orderNo != null) put("orderNo", orderNo)
        if (amount != null) put("amount", amount)
        if (!description.isNullOrBlank()) put("description", description)
    }

    private fun nowIso(): String = Clock.System.now().toString()
}
