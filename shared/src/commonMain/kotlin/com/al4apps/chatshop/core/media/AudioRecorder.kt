package com.al4apps.chatshop.core.media

/**
 * Запись голосовых сообщений. Реализуется на краю приложения (микрофон + разрешение).
 * В shared приходит через интерфейс.
 */
interface AudioRecorder {

    /** Доступна ли запись на платформе (Android — да; iOS пока нет, см. DEBT). */
    val isAvailable: Boolean

    /**
     * Начинает запись (при необходимости запрашивает разрешение на микрофон).
     *
     * @return true, если запись пошла; false — разрешение не выдано / запись недоступна.
     */
    suspend fun start(): Boolean

    /**
     * Останавливает запись и возвращает результат.
     *
     * @return записанное аудио или null, если запись слишком короткая / не велась.
     */
    suspend fun stop(): RecordedAudio?

    /** Отменяет запись без сохранения. */
    fun cancel()
}

/** Максимальная длительность голосового (CLIENT_GUIDE §8: 1..120 сек). */
const val VOICE_MAX_SECONDS: Int = 120
