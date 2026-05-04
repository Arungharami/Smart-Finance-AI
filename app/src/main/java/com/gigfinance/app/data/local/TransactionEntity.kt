package com.gigfinance.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gigfinance.app.data.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["timestamp"]), Index(value = ["type"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val timestamp: Long,
    val note: String
)
