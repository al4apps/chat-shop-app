package com.al4apps.chatshop.core.network

import kotlinx.serialization.Serializable

/** Тело ошибки сервера `application/problem+json` (CLIENT_GUIDE §12). */
@Serializable
data class ProblemJson(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
)
