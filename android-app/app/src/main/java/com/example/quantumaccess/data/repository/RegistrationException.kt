package com.example.quantumaccess.data.repository

/**
 * Domain-specific exception letting the UI know what kind of registration failure occurred.
 */
class RegistrationException(
    val type: Type,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    enum class Type {
        USERNAME_TAKEN,
        EMAIL_TAKEN,
        RATE_LIMITED,
        NETWORK,
        CONFIRMATION_REQUIRED,
        UNKNOWN
    }
}

