package com.al4apps.chatshop.core.media

import android.content.Context
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import java.io.File
import kotlinx.coroutines.withContext

actual class FileStore(
    context: Context,
) {

    private val dir: File = File(context.filesDir, "media").apply { mkdirs() }

    actual suspend fun save(id: String, bytes: ByteArray): String = withContext(AppDispatchers.io) {
        val file = File(dir, id)
        file.writeBytes(bytes)
        file.absolutePath
    }

    actual suspend fun readBytes(id: String): ByteArray? = withContext(AppDispatchers.io) {
        File(dir, id).takeIf { it.exists() }?.readBytes()
    }

    actual fun pathFor(id: String): String = File(dir, id).absolutePath

    actual suspend fun exists(id: String): Boolean = withContext(AppDispatchers.io) {
        File(dir, id).exists()
    }
}
