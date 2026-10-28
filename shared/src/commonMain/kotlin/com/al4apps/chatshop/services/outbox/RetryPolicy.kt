package com.al4apps.chatshop.services.outbox

/**
 * Параметры повторной отправки outbox.
 *
 * Production использует значения по умолчанию: не более трёх попыток и
 * нарастающая задержка 2 и 4 секунды. Тесты передают минимальную задержку,
 * чтобы не ждать реальные интервалы.
 */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val backoffMs: Long = 2_000L,
) {
    init {
        require(maxAttempts > 0) { "maxAttempts must be positive" }
        require(backoffMs >= 0) { "backoffMs must not be negative" }
    }
}
