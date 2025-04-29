package com.example.prog_poe_2025

import Data_Classes.Expenses
import Data_Classes.Income
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application, private val repository: HomeRepository) : AndroidViewModel(application) {

    companion object {
        const val DEFAULT_CURRENCY = "USD"
    }

    // LiveData for selected currency
    private val _currency = MutableLiveData(DEFAULT_CURRENCY)
    val currency: LiveData<String> = _currency

    // LiveData for total income and expenses
    private val _totalIncome = MutableLiveData<Long>()
    val totalIncome: LiveData<Long> = _totalIncome

    private val _totalExpenses = MutableLiveData<Long>()
    val totalExpenses: LiveData<Long> = _totalExpenses

    // LiveData for transactions
    private val _transactions = MutableLiveData<List<TransactionItem>>()
    val getLatestTransactions: LiveData<List<TransactionItem>> = _transactions

    // LiveData for incomes and expenses list if needed
    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> = _incomes

    private val _expenses = MutableLiveData<List<Expenses>>()
    val expenses: LiveData<List<Expenses>> = _expenses

    // Store user ID
    private val userId: Int = getUserIdFromPreferences()



    fun fetchLatestTransactions(preferredCurrency: String) {
        viewModelScope.launch {
            try {
                val transactions = repository.getLatestTransactions(preferredCurrency)
                _transactions.value = transactions
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeViewModel", "Error fetching latest transactions", e)
            }
        }
    }

    fun setCurrency(selectedCurrency: String) {
        _currency.value = selectedCurrency
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                val selectedCurrency = _currency.value ?: DEFAULT_CURRENCY

                Log.d("HomeViewModel", "Refreshing Data -> Currency: $selectedCurrency, User ID: $userId")

                val latestTransactions = repository.getLatestTransactions(selectedCurrency)
                _transactions.value = latestTransactions

                if (latestTransactions.isEmpty()) {
                    Log.d("HomeViewModel", "No transactions found")
                    _transactions.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("HomeViewModel", "Error refreshing data", e)
            }
        }
    }

    private fun getUserIdFromPreferences(): Int {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("user_id", 0)
    }
}

