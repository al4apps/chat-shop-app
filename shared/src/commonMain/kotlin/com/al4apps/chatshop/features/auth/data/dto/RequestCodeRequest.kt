package com.al4apps.chatshop.features.auth.data.dto

import kotlinx.serialization.Serializable

/** Тело `/auth/request-code`. */
@Serializable
data class RequestCodeRequest(
    val phone: String,
)
