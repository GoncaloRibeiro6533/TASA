package com.tasa.domain

data class TasaException(
    val message: String,
    val cause: Throwable? = null,
)
