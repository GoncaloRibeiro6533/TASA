package pt.isel

import java.security.MessageDigest
import java.util.Base64

/**
 * Represents a token encoder that uses SHA-256 to encode the token.
 */
class Sha256TokenEncoder : TokenEncoder {
    /**
     * Creates a [TokenValidationInfo] from a token.
     * @param token the token to encode
     * @return the [TokenValidationInfo] created from the token
     */
    override fun createValidationInformation(token: String): TokenValidationInfo = TokenValidationInfo(hash(token))

    /**
     * Hashes the input using SHA-256.
     * @param input the input to hash
     * @return the hashed input
     */
    private fun hash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA256")
        return Base64.getUrlEncoder().encodeToString(
            messageDigest.digest(
                Charsets.UTF_8.encode(input).array(),
            ),
        )
    }
}
