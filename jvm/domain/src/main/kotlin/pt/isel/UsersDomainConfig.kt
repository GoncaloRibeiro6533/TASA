package pt.isel

import kotlin.time.Duration

/**
 * Represents the users domain configuration defined by its tokenSizeInBytes, tokenTtl, tokenRollingTtl and maxTokensPerUser.
 * @property tokenSizeInBytes the size of the token in bytes
 * @property tokenTtl the time to live of the token
 * @property tokenRollingTtl the rolling time to live of the token
 * @property maxTokensPerUser the maximum number of tokens per user
 * @property refreshTime the time to refresh the token
 */
data class UsersDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerUser: Int,
    val refreshTime: Duration,
) {
    init {
        require(tokenSizeInBytes > 0) { "tokenSizeInBytes must be positive" }
        require(tokenTtl.isPositive()) { "tokenTtl must be positive" }
        require(tokenRollingTtl.isPositive()) { "tokenRollingTtl must be positive" }
        require(maxTokensPerUser > 0) { "maxTokensPerUser must be greater than zero" }
        require(refreshTime.isPositive()) { "refreshTime must be positive" }
    }
}
