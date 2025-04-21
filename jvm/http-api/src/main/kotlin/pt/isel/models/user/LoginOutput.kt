package pt.isel.models.user

import pt.isel.TokenExternalInfo
import pt.isel.User

data class LoginOutput(
    val user: User,
    val session: TokenExternalInfo,
)
