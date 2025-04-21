package pt.isel.models.user

data class UserRegisterInput(
    val username: String,
    val email: String,
    val password: String,
)
