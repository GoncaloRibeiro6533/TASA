package com.tasa.repository.interfaces

import com.tasa.domain.AppExclusion
import com.tasa.domain.ContactExclusion

interface ExceptionRepositoryInterface {
    suspend fun fetchAllAppExceptions(): List<AppExclusion>

    suspend fun fetchAppExceptionById(id: Int): AppExclusion?

    suspend fun fetchAppExceptionByName(name: String): AppExclusion?

    suspend fun insertAppException(exception: AppExclusion)

    suspend fun insertAppExceptions(exceptions: List<AppExclusion>)

    suspend fun deleteAppExceptionById(id: Int)

    suspend fun deleteAppExceptionByName(name: String)

    suspend fun clearAppExceptions()

    suspend fun fetchAllContactExceptions(): List<ContactExclusion>

    suspend fun fetchContactExceptionById(id: Int): ContactExclusion?

    suspend fun fetchContactExceptionByName(name: String): ContactExclusion?

    suspend fun fetchContactExceptionByPhoneNumber(phoneNumber: String): ContactExclusion?

    suspend fun insertContactException(exception: ContactExclusion)

    suspend fun insertContactExceptions(exceptions: List<ContactExclusion>)

    suspend fun deleteContactExceptionById(id: Int)

    suspend fun deleteContactExceptionByName(name: String)

    suspend fun deleteContactExceptionByPhoneNumber(phoneNumber: String)

    suspend fun clearContactExceptions()

    suspend fun clearAllExceptions()
}
