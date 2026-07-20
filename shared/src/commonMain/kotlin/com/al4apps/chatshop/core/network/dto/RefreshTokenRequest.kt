package com.al4apps.chatshop.core.network.dto

import kotlinx.serialization.Serializable

/** Тело `/auth/refresh`. */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
    val device: String? = null,
)
