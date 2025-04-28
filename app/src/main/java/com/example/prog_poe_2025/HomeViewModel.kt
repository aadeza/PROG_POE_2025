package com.example.prog_poe_2025

import Data_Classes.Expenses
import Data_Classes.Income
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch



class HomeViewModel(application: Application,private val repository: HomeRepository) : AndroidViewModel(application) {

    companion object {
        const val DEFAULT_CURRENCY = "USD" // Define a default currency here
    }

    // LiveData for selected currency
    private val _currency = MutableLiveData<String>(DEFAULT_CURRENCY)
    val currency: LiveData<String> = _currency

    // LiveData for incomes and expenses
    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> = _incomes

    private val _expenses = MutableLiveData<List<Expenses>>()
    val expenses: LiveData<List<Expenses>> = _expenses

    // LiveData for totals
    private val _totalIncome = MutableLiveData<Double>()
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    // LiveData for combined transactions (income and expenses)
    private val _transactions = MutableLiveData<List<TransactionItem>>()
    val getLatestTransactions: LiveData<List<TransactionItem>> = _transactions

    fun fetchLatestTransactions(preferredCurrency: String) {
        viewModelScope.launch {
            try {
                val transactions = repository.getLatestTransactions(preferredCurrency)
                _transactions.value = transactions // Update the LiveData
            } catch (e: Exception) {
                e.printStackTrace() // Handle errors gracefully
            }
        }
    }

    fun setCurrency(selectedCurrency: String) {
        _currency.value = selectedCurrency
        refreshData()
    }fun refreshData() {
        viewModelScope.launch {
            try {
                val selectedCurrency = _currency.value ?: DEFAULT_CURRENCY
                val userId = getUserIdFromPreferences()

                Log.d("HomeViewModel", "Selected Currency: $selectedCurrency, User ID: $userId")

                val totalIncome = repository.getTotalIncome(userId, selectedCurrency).toDouble()
                _totalIncome.value = totalIncome

                val totalExpenses = repository.getTotalExpenses(userId, selectedCurrency).toDouble()
                _totalExpenses.value = totalExpenses

                val latestTransactions = repository.getLatestTransactions(selectedCurrency)
                _transactions.value = latestTransactions

                if (latestTransactions.isEmpty()) {
                    Log.d("HomeViewModel", "No transactions found")
                    _transactions.value = listOf()
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
