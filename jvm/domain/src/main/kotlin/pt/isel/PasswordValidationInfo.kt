package pt.isel

data class PasswordValidationInfo(
    val validationInfo: String,
) {
    init {
        require(validationInfo.isNotBlank()) { "password must not be blank" }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 20
    }
}
