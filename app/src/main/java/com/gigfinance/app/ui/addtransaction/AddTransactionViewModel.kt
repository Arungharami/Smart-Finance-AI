package com.gigfinance.app.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gigfinance.app.data.local.TransactionEntity
import com.gigfinance.app.data.model.TransactionType
import com.gigfinance.app.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class AddTransactionUiState(
    val id: Int? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val category: String = "",
    val note: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddTransactionViewModel(
    private val repository: TransactionRepository,
    private val transactionId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        if (transactionId != -1) {
            loadTransaction(transactionId)
        }
    }

    private fun loadTransaction(id: Int) {
        viewModelScope.launch {
            try {
                val transaction = repository.getTransactionById(id)
                if (transaction != null) {
                    _uiState.update {
                        it.copy(
                            id = transaction.id,
                            amount = transaction.amount.toString(),
                            type = transaction.type,
                            category = transaction.category,
                            note = transaction.note
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load transaction") }
            }
        }
    }

    fun updateAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(amount = amount, errorMessage = null) }
        }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { it.copy(type = type, category = "") }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category, errorMessage = null) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        val amountValue = currentState.amount.toDoubleOrNull()
        
        if (amountValue == null || amountValue <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount greater than 0") }
            return
        }
        if (currentState.category.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select a category") }
            return
        }

        viewModelScope.launch {
            try {
                val transaction = TransactionEntity(
                    id = currentState.id ?: 0,
                    amount = amountValue,
                    type = currentState.type,
                    category = currentState.category,
                    note = currentState.note,
                    timestamp = currentState.id?.let { repository.getTransactionById(it)?.timestamp } ?: Date().time
                )
                
                if (currentState.id != null) {
                    repository.updateTransaction(transaction)
                } else {
                    repository.insertTransaction(transaction)
                }
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to save transaction") }
            }
        }
    }

    class Factory(private val repository: TransactionRepository, private val transactionId: Int = -1) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddTransactionViewModel(repository, transactionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
