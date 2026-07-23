package com.al4apps.chatshop.core.media

import com.al4apps.chatshop.core.coroutines.AppDispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

actual suspend fun compressImageToJpeg(bytes: ByteArray, maxBytes: Long): ByteArray =
    withContext(AppDispatchers.io) {
        val image = UIImage.imageWithData(bytes.toNSData()) ?: return@withContext bytes
        var quality = 0.9
        var out = UIImageJPEGRepresentation(image, quality) ?: return@withContext bytes
        while (out.length.toLong() > maxBytes && quality > 0.4) {
            quality -= 0.1
            out = UIImageJPEGRepresentation(image, quality) ?: break
        }
        out.toByteArray()
    }
