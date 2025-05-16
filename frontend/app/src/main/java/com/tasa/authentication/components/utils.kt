package com.tasa.authentication.components

const val MIN_USERNAME_LENGTH = 3
const val MAX_USERNAME_LENGTH = 40

const val MIN_PASSWORD_LENGTH = 4
const val MAX_PASSWORD_LENGTH = 127

const val MAX_EVENT_NAME_LENGTH = 40

const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$"

fun validateUsername(username: String): Boolean = username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH

fun validateEmail(email: String): Boolean = email.matches(EMAIL_REGEX.toRegex())

fun validatePassword(password: String): Boolean = password.length in MIN_PASSWORD_LENGTH..MAX_PASSWORD_LENGTH
