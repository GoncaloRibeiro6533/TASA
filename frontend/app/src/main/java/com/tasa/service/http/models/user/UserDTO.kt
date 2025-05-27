package com.tasa.service.http.models.user

import com.tasa.domain.user.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val username: String,
    val email: String,
) {
    fun toUser() = User(id, username, email)
}
