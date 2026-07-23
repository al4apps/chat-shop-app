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
    /** Файл удалён на сервере по TTL — показать заглушку. */
    val isFileDeleted: Boolean,
    /** Метка времени `14:05`; числовой формат, не текст интерфейса. */
    val time: String,
    val isSending: Boolean,
    val isFailed: Boolean,
)
