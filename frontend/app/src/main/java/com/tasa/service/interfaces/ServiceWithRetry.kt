package com.tasa.service.interfaces

import com.tasa.domain.AuthenticationException
import com.tasa.repository.UserRepository
import com.tasa.utils.Failure

open class ServiceWithRetry(
    private val userRepo: UserRepository,
) {
    suspend fun <T> retryOnFailure(action: suspend () -> T): T {
        var hasTried = false
        return try {
            action()
        } catch (e: AuthenticationException) {
            if (!hasTried) {
                hasTried = true
                val result = userRepo.refreshSession()
                if (result is Failure) {
                    throw e
                } else {
                    action()
                }
            } else {
                throw e
            }
        }
    }
}
