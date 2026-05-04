package com.gigfinance.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gigfinance.app.data.local.TransactionEntity
import com.gigfinance.app.data.model.TransactionType
import com.gigfinance.app.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class SmartFinanceUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val currentInput: String = ""
)

class SmartFinanceViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartFinanceUiState())
    val uiState: StateFlow<SmartFinanceUiState> = _uiState.asStateFlow()

    init {
        // Initial greeting
        addMessage(ChatMessage(text = "Hello! I'm your Smart Finance AI. I can analyze your spending, predict your tax, or spot unusual expenses. What would you like to know?", isUser = false))
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun sendMessage() {
        val userText = _uiState.value.currentInput
        if (userText.isBlank()) return
        
        // Add User Message
        addMessage(ChatMessage(text = userText, isUser = true))
        _uiState.update { it.copy(currentInput = "", isTyping = true) }
        
        // Mock API Call Processing
        viewModelScope.launch {
            delay(1500) // Simulate network delay
            generateResponse(userText.lowercase())
        }
    }

    private suspend fun generateResponse(query: String) {
        // Get start of month
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        val transactions = repository.getTransactionsForMonth(calendar.timeInMillis).firstOrNull() ?: emptyList()
        
        var income = 0.0
        var expenses = 0.0
        val categoryMap = mutableMapOf<String, Double>()

        for (tx in transactions) {
            if (tx.type == TransactionType.INCOME) {
                income += tx.amount
            } else {
                expenses += tx.amount
                categoryMap[tx.category] = (categoryMap[tx.category] ?: 0.0) + tx.amount
            }
        }
        val netProfit = income - expenses
        val taxJar = maxOf(netProfit * 0.25, 0.0)
        val safeToSpend = maxOf(netProfit - taxJar, 0.0)

        val responseText = when {
            query.contains("analyze") || query.contains("spending") -> {
                val highestCategory = categoryMap.maxByOrNull { it.value }
                if (highestCategory != null) {
                    "Based on this month's data, your highest expense is ${highestCategory.key} at $${String.format("%.2f", highestCategory.value)}. You might want to cut back there!"
                } else {
                    "You don't have any recorded expenses yet this month."
                }
            }
            query.contains("tax") || query.contains("predict") -> {
                "Your projected tax savings should be $${String.format("%.2f", taxJar)}. Make sure you keep this in a separate account for IRS payments!"
            }
            query.contains("safe") || query.contains("today") -> {
                "You can safely spend $${String.format("%.2f", safeToSpend)} without cutting into your tax obligations or hitting a deficit."
            }
            query.contains("unusual") || query.contains("detect") -> {
                val unusual = transactions.filter { it.type == TransactionType.EXPENSE && it.amount > 200.0 }
                if (unusual.isNotEmpty()) {
                    "I detected an unusual expense of $${String.format("%.2f", unusual.first().amount)} for ${unusual.first().category}. Make sure this was planned!"
                } else {
                    "All your expenses look normal to me! No unusual spikes detected."
                }
            }
            else -> {
                // OpenAI API hook goes here in the future
                "I'm currently in Beta! I can answer questions about your 'spending', 'tax', 'safe to spend' amounts, or 'unusual' expenses."
            }
        }

        _uiState.update { it.copy(isTyping = false) }
        addMessage(ChatMessage(text = responseText, isUser = false))
    }

    private fun addMessage(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SmartFinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SmartFinanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
