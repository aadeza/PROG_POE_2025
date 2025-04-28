package com.example.prog_poe_2025

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _latestTransactions = MutableLiveData<List<TransactionItem>>()
    val latestTransactions: LiveData<List<TransactionItem>> = _latestTransactions

    init {
        fetchLatestTransactions()
    }

    private fun fetchLatestTransactions() {
        viewModelScope.launch {
            try {
                val latestIncomes = repository.getLatestIncomes(5)   // Fetch 5 latest incomes
                val latestExpenses = repository.getLatestExpenses(5) // Fetch 5 latest expenses

                val combinedTransactions = mutableListOf<TransactionItem>()

                // Map income and expense into TransactionItem types
                combinedTransactions.addAll(latestIncomes.map { TransactionItem.IncomeItem(it) })
                combinedTransactions.addAll(latestExpenses.map { TransactionItem.ExpenseItem(it) })

                // Sort all transactions by date (newest first)
                combinedTransactions.sortByDescending { transactionItem ->
                    when (transactionItem) {
                        is TransactionItem.IncomeItem -> transactionItem.income.date
                        is TransactionItem.ExpenseItem -> transactionItem.expense.date
                    }
                }

                // Take only the 5 most recent ones overall
                _latestTransactions.value = combinedTransactions.take(5)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



}
