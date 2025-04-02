package pt.isel

/**
 * Represents a token encoder.
 */
interface TokenEncoder {
    fun createValidationInformation(token: String): TokenValidationInfo
}
