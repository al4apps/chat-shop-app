package com.al4apps.chatshop.core.media

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioApplication
import platform.AVFAudio.AVAudioApplicationRecordPermissionDenied
import platform.AVFAudio.AVAudioApplicationRecordPermissionGranted
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVEncoderBitRateKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVAudioQualityHigh
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfFile

/**
 * iOS-запись через AVAudioRecorder (AAC в контейнере M4A).
 *
 * Использует общую [IosAudioSession]: на старте переключает сессию в playAndRecord,
 * после остановки возвращает playback, чтобы голосовые сразу воспроизводились.
 */
@OptIn(ExperimentalForeignApi::class)
class IosAudioRecorder : AudioRecorder {

    override val isAvailable: Boolean = true

    private var recorder: AVAudioRecorder? = null
    private var outputPath: String? = null

    override suspend fun start(): Boolean = withContext(Dispatchers.Main) {
        if (recorder != null) return@withContext false
        if (!ensurePermission()) return@withContext false

        IosAudioSession.configureRecording()
        val path = newOutputPath()
        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatMPEG4AAC.toLong(),
            AVSampleRateKey to SAMPLE_RATE,
            AVNumberOfChannelsKey to CHANNELS,
            AVEncoderAudioQualityKey to AVAudioQualityHigh,
            AVEncoderBitRateKey to BITRATE,
        )

        val created = runCatching {
            AVAudioRecorder(
                uRL = NSURL.fileURLWithPath(path),
                settings = settings,
                error = null,
            )
        }.getOrNull()
        if (created == null) {
            IosAudioSession.configurePlayback()
            return@withContext false
        }

        if (!created.prepareToRecord() || !created.record()) {
            created.deleteRecording()
            removeFile(path)
            IosAudioSession.configurePlayback()
            return@withContext false
        }

        recorder = created
        outputPath = path
        true
    }

    override suspend fun stop(): RecordedAudio? = withContext(Dispatchers.Main) {
        val active = recorder ?: return@withContext null
        val path = outputPath
        val durationSec = active.currentTime.toInt()

        active.stop()
        recorder = null
        outputPath = null

        val data = path?.let { NSData.dataWithContentsOfFile(it) }
        path?.let(::removeFile)
        IosAudioSession.configurePlayback()

        if (data == null || durationSec < 1) {
            return@withContext null
        }

        RecordedAudio(
            bytes = data.toByteArray(),
            mime = "audio/mp4",
            durationSec = durationSec.coerceIn(1, VOICE_MAX_SECONDS),
        )
    }

    override fun cancel() {
        val active = recorder ?: return
        active.stop()
        recorder = null
        outputPath?.let(::removeFile)
        outputPath = null
        IosAudioSession.configurePlayback()
    }

    private suspend fun ensurePermission(): Boolean {
        val application = AVAudioApplication.sharedInstance
        return when (application.recordPermission) {
            AVAudioApplicationRecordPermissionGranted -> true
            AVAudioApplicationRecordPermissionDenied -> false
            else -> {
                val result = CompletableDeferred<Boolean>()
                AVAudioApplication.requestRecordPermissionWithCompletionHandler { granted ->
                    result.complete(granted)
                }
                result.await()
            }
        }
    }

    private fun newOutputPath(): String {
        val directory = NSTemporaryDirectory().trimEnd('/')
        return "$directory/voice-${NSUUID().UUIDString}.m4a"
    }

    private fun removeFile(path: String) {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    }

    private companion object {
        const val SAMPLE_RATE = 44_100.0
        const val CHANNELS = 1L
        const val BITRATE = 96_000L
    }
}
