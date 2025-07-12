package com.tasa.service.interfaces

import com.tasa.domain.AuthenticationException
import com.tasa.repository.UserRepository
import com.tasa.utils.Failure
import com.tasa.utils.Success

/**
 * ServiceWithRetry is a base class for services that need to handle authentication
 * and retry logic when making API calls.
 * It provides a method to execute an action with a retry mechanism
 * in case of authentication failures.
 */
open class ServiceWithRetry(
    private val userRepo: UserRepository,
) {
    /**
     * Executes the given action with the current user token.
     * If an AuthenticationException occurs, it attempts to refresh the session
     * and retries the action with the new token.
     *
     * @param action The action to be executed, which takes a token as a parameter.
     * @return The result of the action.
     * @throws AuthenticationException if both attempts fail.
     */
    suspend fun <T> retryOnFailure(action: suspend (token: String) -> T): T {
        val firstToken = userRepo.getToken()
        return try {
            action(firstToken)
        } catch (e: AuthenticationException) {
            val refreshResult = userRepo.refreshSession()

            if (refreshResult is Failure) {
                throw e
            }

            val newToken = (refreshResult as Success).value

            try {
                action(newToken)
            } catch (e2: AuthenticationException) {
                throw e2
            }
        }
    }
}
