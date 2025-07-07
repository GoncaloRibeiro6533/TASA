package com.tasa.service.http.models.user

import kotlinx.serialization.Serializable

@Serializable
data class TokenInput(
    val token: String,
    val refreshToken: String,
)
