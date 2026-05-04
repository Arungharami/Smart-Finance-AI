package com.gigfinance.app.repository

import com.gigfinance.app.data.local.TransactionDao
import com.gigfinance.app.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsForMonth(startOfMonth: Long): Flow<List<TransactionEntity>> = 
        transactionDao.getTransactionsForMonth(startOfMonth)

    suspend fun getTransactionById(id: Int): TransactionEntity? = 
        transactionDao.getTransactionById(id)

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
}
