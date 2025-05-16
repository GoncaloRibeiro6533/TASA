package pt.isel

import org.jdbi.v3.core.Jdbi
import pt.isel.transaction.Transaction
import pt.isel.transaction.TransactionManager

class TransactionManagerJdbi(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: Transaction.() -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = TransactionJdbi(handle)
            block(transaction)
        }
}
