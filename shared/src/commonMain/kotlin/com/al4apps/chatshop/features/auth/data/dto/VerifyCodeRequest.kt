package com.al4apps.chatshop.features.auth.data.dto

import kotlinx.serialization.Serializable

/** Тело `/auth/verify`. */
@Serializable
data class VerifyCodeRequest(
    val phone: String,
    val code: String,
    val device: String? = null,
)
