package pt.isel.models.user

data class UserLoginCredentialsInput(
    val username: String,
    val password: String,
)
