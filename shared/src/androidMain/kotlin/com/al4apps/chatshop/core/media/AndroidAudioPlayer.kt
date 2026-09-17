package com.al4apps.chatshop.core.media

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidAudioPlayer : AudioPlayer {

    private val mutableState = MutableStateFlow(AudioPlaybackState())
    override val state: StateFlow<AudioPlaybackState> = mutableState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: MediaPlayer? = null
    private var tickerJob: Job? = null

    override fun toggle(id: String, filePath: String, durationSec: Int) {
        val current = mutableState.value
        when {
            current.playingId == id && current.isPlaying -> pause()
            current.playingId == id && !current.isPlaying -> resume()
            else -> startNew(id, filePath)
        }
    }

    override fun stop() {
        releasePlayer(stopPlayback = true)
        mutableState.value = AudioPlaybackState()
    }

    private fun startNew(id: String, filePath: String) {
        stop()
        val created = runCatching {
            MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
            }
        }.getOrNull() ?: return

        // Слушатели навешиваются до start() и с явным параметром плеера: неквалифицированный
        // stop() внутри лямбды резолвился бы в MediaPlayer.stop() (shadowing receiver из apply).
        created.setOnCompletionListener { ended -> onPlaybackEnded(ended) }
        created.setOnErrorListener { failed, _, _ ->
            onPlaybackEnded(failed)
            true
        }

        if (!runCatching { created.start() }.isSuccess) {
            runCatching { created.release() }
            return
        }
        player = created
        mutableState.value = AudioPlaybackState(playingId = id, isPlaying = true, positionSec = 0)
        startTicker()
    }

    /** Завершение или ошибка плеера: сброс состояния и освобождение без повторного stop(). */
    private fun onPlaybackEnded(ended: MediaPlayer) {
        if (player !== ended) return
        player = null
        tickerJob?.cancel()
        tickerJob = null
        runCatching { ended.release() }
        mutableState.value = AudioPlaybackState()
    }

    private fun pause() {
        tickerJob?.cancel()
        tickerJob = null
        runCatching { player?.pause() }
        mutableState.value = mutableState.value.copy(isPlaying = false)
    }

    private fun resume() {
        runCatching { player?.start() }
        mutableState.value = mutableState.value.copy(isPlaying = true)
        startTicker()
    }

    private fun releasePlayer(stopPlayback: Boolean) {
        tickerJob?.cancel()
        tickerJob = null
        val active = player ?: return
        player = null
        if (stopPlayback) runCatching { active.stop() }
        runCatching { active.release() }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val positionSec = (player?.currentPosition ?: 0) / 1000
                mutableState.value = mutableState.value.copy(positionSec = positionSec)
                delay(500)
            }
        }
    }
}
