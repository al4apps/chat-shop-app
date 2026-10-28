package com.al4apps.chatshop.core.media

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionAllowBluetooth
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive

/**
 * Единая настройка AVAudioSession для записи и воспроизведения.
 *
 * iOS-сессия глобальная: после записи её нужно вернуть в playback-режим,
 * иначе голосовое может уйти в разговорный динамик или не запуститься.
 */
@OptIn(ExperimentalForeignApi::class)
internal object IosAudioSession {

    /** Режим обычного воспроизведения голосовых. */
    fun configurePlayback() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            category = AVAudioSessionCategoryPlayback,
            mode = AVAudioSessionModeDefault,
            options = 0uL,
            error = null,
        )
        session.setActive(true, error = null)
    }

    /** Режим записи с выводом на основной динамик. */
    fun configureRecording() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(
            category = AVAudioSessionCategoryPlayAndRecord,
            mode = AVAudioSessionModeDefault,
            options = AVAudioSessionCategoryOptionDefaultToSpeaker or
                AVAudioSessionCategoryOptionAllowBluetooth,
            error = null,
        )
        session.setActive(true, error = null)
    }

    /** Освобождает аудиосессию, если она больше не нужна. */
    fun deactivate() {
        AVAudioSession.sharedInstance().setActive(false, error = null)
    }
}
