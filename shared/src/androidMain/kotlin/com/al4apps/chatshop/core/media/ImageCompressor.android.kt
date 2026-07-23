package com.al4apps.chatshop.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.al4apps.chatshop.core.coroutines.AppDispatchers
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.withContext

actual suspend fun compressImageToJpeg(bytes: ByteArray, maxBytes: Long): ByteArray =
    withContext(AppDispatchers.io) {
        val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: return@withContext bytes
        var bitmap = source.downscaleIfNeeded(MAX_DIMENSION)

        var quality = 90
        var out = bitmap.toJpeg(quality)
        // Подбираем качество, затем масштаб — пока не уложимся в лимит.
        while (out.size > maxBytes && quality > MIN_QUALITY) {
            quality -= 10
            out = bitmap.toJpeg(quality)
        }
        while (out.size > maxBytes) {
            bitmap = bitmap.downscaleIfNeeded(bitmap.width / 2)
            out = bitmap.toJpeg(quality)
            if (bitmap.width <= MIN_DIMENSION) break
        }
        out
    }

private fun Bitmap.toJpeg(quality: Int): ByteArray = ByteArrayOutputStream().use { stream ->
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    stream.toByteArray()
}

private fun Bitmap.downscaleIfNeeded(maxSide: Int): Bitmap {
    val largest = maxOf(width, height)
    if (largest <= maxSide) return this
    val scale = maxSide.toFloat() / largest
    return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
}

private const val MAX_DIMENSION = 2048
private const val MIN_DIMENSION = 256
private const val MIN_QUALITY = 40
