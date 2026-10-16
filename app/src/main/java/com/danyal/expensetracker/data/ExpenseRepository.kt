package com.danyal.expensetracker.data

import kotlinx.coroutines.flow.Flow

/** Single place the rest of the app talks to for reading and writing data. */
class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.observeAll()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.observeAll()

    suspend fun allTransactions(): List<TransactionEntity> = transactionDao.getAll()

    suspend fun get(id: Long): TransactionEntity? = transactionDao.getById(id)

    /** Inserts a new transaction (id == 0) or updates an existing one. */
    suspend fun save(transaction: TransactionEntity) {
        if (transaction.id == 0L) transactionDao.insert(transaction) else transactionDao.update(transaction)
    }

    /** Re-inserts a deleted transaction with its original id (used by "Undo"). */
    suspend fun restore(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    suspend fun saveBudget(budget: BudgetEntity) = budgetDao.upsert(budget)

    suspend fun deleteBudget(categoryId: String) = budgetDao.delete(categoryId)

    suspend fun clearAll() {
        transactionDao.clear()
        budgetDao.clear()
    }
}
