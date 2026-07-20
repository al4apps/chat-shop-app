package com.al4apps.chatshop.core.network.dto

import kotlinx.serialization.Serializable

/** Ответ `/auth/verify` и `/auth/refresh` (CLIENT_GUIDE §3). */
@Serializable
data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
    val expiresIn: Long,
)
