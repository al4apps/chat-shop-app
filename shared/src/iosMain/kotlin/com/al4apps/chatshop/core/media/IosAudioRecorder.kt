package com.al4apps.chatshop.core.media

/**
 * iOS-запись голосовых — заглушка до реализации AVAudioRecorder (см. DEBT).
 * Приложение тестируется на Android-эмуляторе.
 */
class IosAudioRecorder : AudioRecorder {

    override val isAvailable: Boolean = false

    override suspend fun start(): Boolean = false

    override suspend fun stop(): RecordedAudio? = null

    override fun cancel() = Unit
}
