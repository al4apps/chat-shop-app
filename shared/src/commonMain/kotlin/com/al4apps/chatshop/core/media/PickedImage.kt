package com.al4apps.chatshop.core.media

/** Выбранное пользователем изображение (сырые байты из системного пикера). */
data class PickedImage(
    val bytes: ByteArray,
    val mime: String,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (other is PickedImage && mime == other.mime && bytes.contentEquals(other.bytes))

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + mime.hashCode()
}
