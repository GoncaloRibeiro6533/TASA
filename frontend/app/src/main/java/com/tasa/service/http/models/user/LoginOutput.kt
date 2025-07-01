package com.tasa.service.http.models.user

import com.tasa.domain.user.User
import java.time.LocalDateTime

data class LoginOutput(
    val user: User,
    val session: TokenExternalInfo,
)

data class TokenExternalInfo(
    val token: String,
    val refreshToken: String,
    val expiration: LocalDateTime,
)
