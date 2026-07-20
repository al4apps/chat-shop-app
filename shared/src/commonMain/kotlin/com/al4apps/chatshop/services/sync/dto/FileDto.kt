package com.al4apps.chatshop.services.sync.dto

import kotlinx.serialization.Serializable

/**
 * Вложение (CLIENT_GUIDE §4). `url` — временная presigned-ссылка, в БД не сохраняется;
 * скачивание файлов — этапы 9–10.
 */
@Serializable
data class FileDto(
    val id: String,
    val mime: String,
    val size: Long,
    val durationSec: Int? = null,
    val url: String? = null,
    val status: String,
)
