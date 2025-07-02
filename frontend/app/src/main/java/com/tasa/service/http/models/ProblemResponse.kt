package com.tasa.service.http.models

data class ProblemResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
)
