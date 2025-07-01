package com.tasa.service.http.models.user

data class UserRegisterInput(
    val username: String,
    val email: String,
    val password: String,
)
