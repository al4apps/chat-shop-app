package com.al4apps.chatshop.core.media

/** Записанное голосовое: сжатые байты (AAC/M4A), mime и реальная длительность в секундах. */
data class RecordedAudio(
    val bytes: ByteArray,
    val mime: String,
    val durationSec: Int,
) {
    override fun equals(other: Any?): Boolean =
        this === other || (
            other is RecordedAudio &&
                durationSec == other.durationSec &&
                mime == other.mime &&
                bytes.contentEquals(other.bytes)
            )

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mime.hashCode()
        result = 31 * result + durationSec
        return result
    }
}
