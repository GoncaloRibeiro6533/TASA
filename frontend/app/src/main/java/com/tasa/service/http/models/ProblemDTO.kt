package com.tasa.service.http.models

import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class ProblemDTO(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
) {
    fun toProblem() =
        ProblemResponse(
            type = URI.create(type).toString(),
            title = title,
            status = status,
            detail = detail,
        )
}
