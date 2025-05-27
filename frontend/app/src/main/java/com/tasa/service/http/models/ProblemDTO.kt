package com.tasa.service.http.models

import com.tasa.domain.Problem
import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class ProblemDTO(
    val type: String,
) {
    fun toProblem() = Problem(URI(type))
}
