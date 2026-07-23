package com.al4apps.chatshop.core.media

/**
 * Сжимает изображение в JPEG не больше [maxBytes] (CLIENT_GUIDE §8: IMAGE ≤ 5 МБ).
 * Платформенная реализация подбирает качество/масштаб.
 */
expect suspend fun compressImageToJpeg(bytes: ByteArray, maxBytes: Long): ByteArray

/** Лимит размера изображения на сервере (5 МБ). */
const val IMAGE_MAX_BYTES: Long = 5L * 1024 * 1024
