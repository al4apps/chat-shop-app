package com.al4apps.chatshop.core.media

import kotlinx.cinterop.ExperimentalForeignApi
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
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalForeignApi::class)
class IosAudioPlayer : AudioPlayer {

    private val mutableState = MutableStateFlow(AudioPlaybackState())
    override val state: StateFlow<AudioPlaybackState> = mutableState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: AVAudioPlayer? = null
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
        tickerJob?.cancel()
        runCatching { player?.stop() }
        player = null
        mutableState.value = AudioPlaybackState()
    }

    private fun startNew(id: String, filePath: String) {
        stop()
        IosAudioSession.configurePlayback()
        val url = NSURL.fileURLWithPath(filePath)
        val created = AVAudioPlayer(contentsOfURL = url, error = null)
        val prepared = created.prepareToPlay() && created.play()
        if (!prepared) return
        player = created
        mutableState.value = AudioPlaybackState(playingId = id, isPlaying = true, positionSec = 0)
        startTicker()
    }

    private fun pause() {
        runCatching { player?.pause() }
        tickerJob?.cancel()
        mutableState.value = mutableState.value.copy(isPlaying = false)
    }

    private fun resume() {
        runCatching { player?.play() }
        mutableState.value = mutableState.value.copy(isPlaying = true)
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val activePlayer = player
                if (activePlayer != null && !activePlayer.playing) {
                    stop()
                    break
                }
                val positionSec = activePlayer?.currentTime?.toInt() ?: 0
                mutableState.value = mutableState.value.copy(positionSec = positionSec)
                delay(500.milliseconds)
            }
        }
    }
}
