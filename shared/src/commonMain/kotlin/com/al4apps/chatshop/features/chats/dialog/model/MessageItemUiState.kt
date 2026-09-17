package com.al4apps.chatshop.features.chats.dialog.model

import androidx.compose.runtime.Immutable

@Immutable
data class MessageItemUiState(
    val localId: Long,
    val text: String,
    val isMine: Boolean,
    val isSystem: Boolean,
    val isImage: Boolean,
    /** Локальный путь к картинке (своя или скачанная); null — ещё нет байтов. */
    val imagePath: String?,
    val isVoice: Boolean,
    /** Идентификатор файла голосового (для плеера); null у не-голосовых. */
    val voiceFileId: String?,
    /** Локальный путь к голосовому; null — ещё не скачано (нельзя играть). */
    val voicePath: String?,
    val voiceDurationSec: Int,
    val isVoicePlaying: Boolean,
    val voicePositionSec: Int,
    /** Файл удалён на сервере по TTL — показать заглушку. */
    val isFileDeleted: Boolean,
    /** Метка времени `14:05`; числовой формат, не текст интерфейса. */
    val time: String,
    val isSending: Boolean,
    val isFailed: Boolean,
)
