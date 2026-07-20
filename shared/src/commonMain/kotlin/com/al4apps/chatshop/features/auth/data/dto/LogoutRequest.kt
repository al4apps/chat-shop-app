package com.al4apps.chatshop.features.auth.data.dto

import kotlinx.serialization.Serializable

/** Тело `/auth/logout`. */
@Serializable
data class LogoutRequest(
    val refreshToken: String,
)
