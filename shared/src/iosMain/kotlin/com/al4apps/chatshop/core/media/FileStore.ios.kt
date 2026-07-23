package com.al4apps.chatshop.core.media

import com.al4apps.chatshop.core.coroutines.AppDispatchers
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual class FileStore {

    private val dir: String = run {
        val docs = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true,
        ).first() as String
        val media = "$docs/media"
        NSFileManager.defaultManager.createDirectoryAtPath(media, true, null, null)
        media
    }

    actual suspend fun save(id: String, bytes: ByteArray): String = withContext(AppDispatchers.io) {
        val path = pathFor(id)
        bytes.toNSData().writeToFile(path, atomically = true)
        path
    }

    actual suspend fun readBytes(id: String): ByteArray? = withContext(AppDispatchers.io) {
        NSData.dataWithContentsOfFile(pathFor(id))?.toByteArray()
    }

    actual fun pathFor(id: String): String = "$dir/$id"

    actual suspend fun exists(id: String): Boolean = withContext(AppDispatchers.io) {
        NSFileManager.defaultManager.fileExistsAtPath(pathFor(id))
    }
}
