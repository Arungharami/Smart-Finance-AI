package com.gigfinance.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gigfinance.app.data.local.TransactionEntity
import com.gigfinance.app.data.model.TransactionType
import com.gigfinance.app.monetization.PremiumManager
import com.gigfinance.app.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryStat(val category: String, val amount: Double)

data class DashboardUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val taxJar: Double = 0.0,
    val safeToSpend: Double = 0.0,
    val categoryBreakdown: List<CategoryStat> = emptyList(),
    val isPremium: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val repository: TransactionRepository,
    private val premiumManager: PremiumManager = PremiumManager()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Get current month start
    private val startOfMonth: Long
        get() {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getTransactionsForMonth(startOfMonth),
        premiumManager.isPremium,
        _isLoading,
        _errorMessage
    ) { transactions, isPremium, isLoading, errorMessage ->
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

        val breakdown = categoryMap.map { CategoryStat(it.key, it.value) }
            .sortedByDescending { it.amount }

        DashboardUiState(
            transactions = transactions,
            totalIncome = income,
            totalExpenses = expenses,
            netProfit = netProfit,
            taxJar = taxJar,
            safeToSpend = safeToSpend,
            categoryBreakdown = breakdown,
            isPremium = isPremium,
            isLoading = false,
            errorMessage = errorMessage
        )
    }.catch { e ->
        _errorMessage.value = e.message
        _isLoading.value = false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete transaction"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
