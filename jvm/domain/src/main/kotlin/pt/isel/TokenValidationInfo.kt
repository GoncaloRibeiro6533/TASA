package pt.isel

/**
 * Represents token validation information.
 * @property validationInfo the token validation information
 * @throws IllegalArgumentException if the token is invalid
 */
data class TokenValidationInfo(
    val validationInfo: String,
) {
    init {
        require(validationInfo.isNotBlank()) { "token must not be blank" }
    }
}
