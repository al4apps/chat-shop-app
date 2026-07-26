package com.al4apps.chatshop.core.media

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Транзиентное состояние записи для UI. */
data class VoiceRecordingState(
    val isRecording: Boolean = false,
    val seconds: Int = 0,
)

/**
 * Управляет записью голосового: старт/стоп/отмена, тикающий таймер и авто-стоп на лимите.
 * Готовую запись отдаёт через [onCompleted]. Переиспользуется диалогом и первым заказом.
 */
class VoiceRecordingController(
    private val recorder: AudioRecorder,
    private val scope: CoroutineScope,
    private val onCompleted: suspend (RecordedAudio) -> Unit,
) {

    private val mutableState = MutableStateFlow(VoiceRecordingState())
    val state: StateFlow<VoiceRecordingState> = mutableState.asStateFlow()

    val isAvailable: Boolean get() = recorder.isAvailable

    private var timerJob: Job? = null

    fun start() {
        if (mutableState.value.isRecording) return
        scope.launch {
            if (!recorder.start()) return@launch
            mutableState.value = VoiceRecordingState(isRecording = true, seconds = 0)
            timerJob = scope.launch {
                while (isActive) {
                    delay(1_000)
                    val next = mutableState.value.seconds + 1
                    mutableState.value = mutableState.value.copy(seconds = next)
                    if (next >= VOICE_MAX_SECONDS) {
                        stop()
                        break
                    }
                }
            }
        }
    }

    fun stop() {
        if (!mutableState.value.isRecording) return
        timerJob?.cancel()
        mutableState.value = VoiceRecordingState()
        scope.launch {
            val audio = recorder.stop() ?: return@launch
            onCompleted(audio)
        }
    }

    fun cancel() {
        timerJob?.cancel()
        mutableState.value = VoiceRecordingState()
        recorder.cancel()
    }
}
