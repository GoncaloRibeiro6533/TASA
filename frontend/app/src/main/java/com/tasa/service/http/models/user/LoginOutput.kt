package com.tasa.service.http.models.user

import com.tasa.domain.user.User
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class LoginOutput(
    val user: User,
    val session: TokenExternalInfo,
)

@Serializable
data class TokenExternalInfo(
    val token: String,
    val refreshToken: String,
    @Contextual
    val expiration: LocalDateTime,
)
