package com.al4apps.chatshop.core.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import java.io.File
import kotlin.time.TimeSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext

/**
 * Android-запись через MediaRecorder (AAC в контейнере MPEG-4/M4A). App-scope singleton,
 * мостящий к Activity для запроса разрешения RECORD_AUDIO: Activity в onCreate вызывает
 * [attach], в onDestroy — [detach]; результат разрешения передаётся в [deliverPermission].
 */
class AndroidAudioRecorder(
    private val context: Context,
) : AudioRecorder {

    override val isAvailable: Boolean = true

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startMark: TimeSource.Monotonic.ValueTimeMark? = null

    private var requestPermission: (() -> Unit)? = null
    private var permissionPending: CompletableDeferred<Boolean>? = null

    fun attach(requestPermission: () -> Unit) {
        this.requestPermission = requestPermission
    }

    fun detach() {
        requestPermission = null
        permissionPending?.complete(false)
        permissionPending = null
    }

    /** Вызывается Activity с результатом запроса разрешения на микрофон. */
    fun deliverPermission(granted: Boolean) {
        permissionPending?.complete(granted)
        permissionPending = null
    }

    override suspend fun start(): Boolean {
        if (!ensurePermission()) return false
        return withContext(AppDispatchers.io) {
            runCatching {
                val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
                val mediaRecorder = createRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(BITRATE)
                    setAudioSamplingRate(SAMPLE_RATE)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                recorder = mediaRecorder
                outputFile = file
                startMark = TimeSource.Monotonic.markNow()
            }.isSuccess
        }
    }

    override suspend fun stop(): RecordedAudio? = withContext(AppDispatchers.io) {
        val activeRecorder = recorder ?: return@withContext null
        val file = outputFile
        val elapsedSec = startMark?.elapsedNow()?.inWholeSeconds?.toInt() ?: 0
        val stopped = runCatching {
            activeRecorder.stop()
        }.isSuccess
        activeRecorder.release()
        recorder = null
        outputFile = null
        startMark = null

        if (!stopped || file == null || !file.exists() || elapsedSec < 1) {
            file?.delete()
            return@withContext null
        }
        val bytes = file.readBytes()
        file.delete()
        RecordedAudio(
            bytes = bytes,
            mime = "audio/mp4",
            durationSec = elapsedSec.coerceAtMost(VOICE_MAX_SECONDS),
        )
    }

    override fun cancel() {
        runCatching { recorder?.stop() }
        recorder?.release()
        recorder = null
        outputFile?.delete()
        outputFile = null
        startMark = null
    }

    private suspend fun ensurePermission(): Boolean {
        if (hasPermission()) return true
        val request = requestPermission ?: return false
        val deferred = CompletableDeferred<Boolean>()
        permissionPending = deferred
        request()
        return deferred.await()
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

    private companion object {
        const val BITRATE = 96_000
        const val SAMPLE_RATE = 44_100
    }
}
