package com.tasa.service.http.models.user

data class UserLoginCredentialsInput(
    val username: String,
    val password: String,
)
