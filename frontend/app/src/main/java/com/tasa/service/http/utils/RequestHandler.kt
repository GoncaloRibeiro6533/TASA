package com.tasa.service.http.utils

import com.tasa.TasaApplication
import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.TasaException
import com.tasa.service.http.models.ProblemResponse
import com.tasa.utils.Either
import com.tasa.utils.failure
import com.tasa.utils.success
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import java.util.Locale

const val MEDIA_TYPE = "application/json"
val BASE_URL = "${TasaApplication.Companion.apiUrl}/api"
const val ERROR_MEDIA_TYPE = "application/problem+json"
const val SCHEME = "bearer"
const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
var language = Locale.getDefault().language.split('_').firstOrNull() ?: "en"

// Main GET request function
suspend inline fun <reified T : Any> HttpClient.get(
    url: String,
    token: String = "",
): Either<ApiError, T> {
    return try {
        get(BASE_URL + url) {
            if (token.isNotEmpty()) header("Authorization", "$SCHEME $token")
            header("Content-Type", MEDIA_TYPE)
            header("Accept", "$MEDIA_TYPE, $ERROR_MEDIA_TYPE")
            header("Accept-Language", language)
        }.processResponse()
    } catch (e: Throwable) {
        failure(ApiError("Unexpected error: ${e.message ?: e.cause?.message}"))
    }
}

suspend inline fun <reified T : Any> HttpClient.post(
    url: String,
    token: String = "",
    body: Any? = null,
): Either<ApiError, T> {
    return post(BASE_URL + url) {
        if (token.isNotEmpty()) header("Authorization", "$SCHEME $token")
        header("Content-Type", MEDIA_TYPE)
        header("Accept", "$MEDIA_TYPE, $ERROR_MEDIA_TYPE")
        header("Accept-Language", language)
        if (body != null) setBody(body)
    }.processResponse()
}

suspend inline fun <reified T : Any> HttpClient.put(
    url: String,
    token: String = "",
    body: Any? = null,
): Either<ApiError, T> {
    return put(BASE_URL + url) {
        if (token.isNotEmpty()) header("Authorization", "$SCHEME $token")
        header("Content-Type", MEDIA_TYPE)
        header("Accept", "$MEDIA_TYPE, $ERROR_MEDIA_TYPE")
        header("Accept-Language", language)
        if (body != null) setBody(body)
    }.processResponse()
}

suspend inline fun <reified T : Any> HttpClient.delete(
    url: String,
    token: String = "",
): Either<ApiError, T> {
    return delete(BASE_URL + url) {
        if (token.isNotEmpty()) header("Authorization", "$SCHEME $token")
        header("Content-Type", MEDIA_TYPE)
        header("Accept", "$MEDIA_TYPE, $ERROR_MEDIA_TYPE")
        header("Accept-Language", language)
    }.processResponse()
}

// Function to process the HTTP response
suspend inline fun <reified T : Any> HttpResponse.processResponse(): Either<ApiError, T> {
    try {
        if (
            this.status.value == 200 &&
            this.headers[HttpHeaders.ContentLength]?.toInt() == 0
        ) {
            return success(Unit as T)
        }
        if (this.headers[NAME_WWW_AUTHENTICATE_HEADER] != null && this.status.value == 401) {
            throw AuthenticationException(
                "Failed to authenticate",
                null,
            )
        }
        // TODO check for the utf-8 on server properties
        val a = this.headers[HttpHeaders.ContentType]?.split(';')?.first()
        return when (a) {
            ERROR_MEDIA_TYPE -> {
                val problem: ProblemResponse = this.body<ProblemResponse>()
                failure(ApiError(problem.detail))
            }
            MEDIA_TYPE -> {
                val body: T = this.body<T>()
                success(body)
            }
            else -> {
                throw TasaException("Unexpected error", null)
            }
        }
    } catch (e: Throwable) {
        throw e
    }
}
