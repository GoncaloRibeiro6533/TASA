package com.tasa.service.http.models

import kotlinx.serialization.Serializable

@Serializable
data class ProblemResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
)
