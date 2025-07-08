package com.tasa.service.interfaces

import com.tasa.domain.AuthenticationException
import com.tasa.repository.UserRepository
import com.tasa.utils.Failure
import com.tasa.utils.Success

open class ServiceWithRetry(
    private val userRepo: UserRepository,
) {
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
