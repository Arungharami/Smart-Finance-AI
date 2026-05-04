package com.gigfinance.app

import android.app.Application
import com.gigfinance.app.data.local.AppDatabase
import com.gigfinance.app.repository.TransactionRepository

class GigFinanceApplication : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
