package com.tasa.domain

class TasaException(
    message: String,
    cause: Throwable? = null,
) : Throwable(message, cause) {
    override fun toString(): String {
        return "TasaException(message='$message', cause=$cause)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TasaException) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
