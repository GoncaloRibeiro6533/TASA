package pt.isel

import pt.isel.transaction.TransactionManager

sealed class ExclusionError {
    data object AppNameTooLong : ExclusionError()

    data object AppNameBlank : ExclusionError()

    data object UserNotFound : ExclusionError()

    data object ExclusionAlreadyExists : ExclusionError()

    data object ExclusionNotFound : ExclusionError()
}

class ExclusionService(
    private val trxManager: TransactionManager,
)
