package com.al4apps.chatshop.core.media

import kotlinx.coroutines.flow.StateFlow

/** Состояние воспроизведения (один плеер на приложение — играет одно голосовое). */
data class AudioPlaybackState(
    /** Идентификатор играющего файла (file.id); null — ничего не играет. */
    val playingId: String? = null,
    val isPlaying: Boolean = false,
    val positionSec: Int = 0,
)

/**
 * Воспроизведение голосовых из локального кэша. Реализуется на краю приложения
 * (MediaPlayer / AVAudioPlayer), в shared приходит через интерфейс.
 */
interface AudioPlayer {

    val state: StateFlow<AudioPlaybackState>

    /**
     * Переключает воспроизведение файла [id] по пути [filePath]:
     * играет этот же — ставит на паузу; на паузе — возобновляет; другой — начинает заново.
     */
    fun toggle(id: String, filePath: String, durationSec: Int)

    /** Останавливает воспроизведение (например, при уходе с экрана). */
    fun stop()
}
