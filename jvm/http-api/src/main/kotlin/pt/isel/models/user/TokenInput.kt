package pt.isel.models.user

data class TokenInput(
    val token: String,
    val refreshToken: String,
)
