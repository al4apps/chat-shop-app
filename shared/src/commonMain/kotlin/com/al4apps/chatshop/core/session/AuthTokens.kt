package com.al4apps.chatshop.core.session

import kotlinx.serialization.Serializable

/** Пара токенов сессии. Refresh — rotating: пара всегда сохраняется атомарно целиком. */
@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
