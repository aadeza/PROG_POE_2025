package com.example.prog_poe_2025

import Data_Classes.Expenses
import Data_Classes.Income
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class HomeViewModel(
    application: Application,
    private val repository: HomeRepository
) : AndroidViewModel(application) {

    companion object {
        const val DEFAULT_CURRENCY = "USD"
    }

    private val _userId = MutableLiveData<Int>()
    val userId: LiveData<Int> = _userId

    private val _currency = MutableLiveData(DEFAULT_CURRENCY)
    val currency: LiveData<String> = _currency

    private val _totalIncome = MutableLiveData<Long>(0L)
    val totalIncome: LiveData<Long> = _totalIncome

    private val _totalExpenses = MutableLiveData<Long>(0L)
    val totalExpenses: LiveData<Long> = _totalExpenses

    private val _netSavings = MutableLiveData<Long>()
    val netSavings: LiveData<Long> = _netSavings

    private val _transactions = MutableLiveData<List<TransactionItem>>()
    val latestTransactions: LiveData<List<TransactionItem>> = _transactions

    init {
        val storedUserId = getUserIdFromPreferences()
        if (storedUserId != -1) {
            setUserId(storedUserId)
        } else {
            Log.w("HomeViewModel", "User ID not found in SharedPreferences.")
        }
    }

    fun setUserId(id: Int) {
        if (_userId.value != id) {
            _userId.value = id
            loadFinancialData()
        }
    }

    fun setCurrency(selectedCurrency: String) {
        if (_currency.value != selectedCurrency) {
            _currency.value = selectedCurrency
            calculateNetSavings()
            fetchLatestTransactions(selectedCurrency)
        }
    }

    private fun loadFinancialData() {
        val userId = _userId.value ?: return

        viewModelScope.launch {
            try {
                val income = repository.getTotalIncome(userId) ?: 0L
                val expenses = repository.getTotalExpenses(userId) ?: 0L

                _totalIncome.postValue(income)
                _totalExpenses.postValue(expenses)

                calculateNetSavings()
                fetchLatestTransactions(_currency.value ?: DEFAULT_CURRENCY)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading financial data", e)
            }
        }
    }

    private fun calculateNetSavings() {
        val income = _totalIncome.value ?: 0L
        val expenses = _totalExpenses.value ?: 0L
        val difference = income - expenses
        val fromCurrency = "ZAR"
        val toCurrency = _currency.value ?: DEFAULT_CURRENCY

        viewModelScope.launch {
            try {
                val converted = CurrencyConverter.convertAmount(difference, fromCurrency, toCurrency)
                _netSavings.postValue(converted.roundToLong())
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Currency conversion failed: ${e.message}")
                _netSavings.postValue(difference)
            }
        }
    }

    fun fetchLatestTransactions(preferredCurrency: String) {
        viewModelScope.launch {
            try {
                val transactions = repository.getLatestTransactions(preferredCurrency)
                _transactions.postValue(transactions)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching latest transactions", e)
            }
        }
    }

    fun refreshData() {
        loadFinancialData()
    }

    private fun getUserIdFromPreferences(): Int {
        val sharedPrefs = getApplication<Application>()
            .getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("user_id", -1)
    }

    private val _allTransactions = MutableLiveData<List<TransactionItem>>()
    val allTransactions: LiveData<List<TransactionItem>> = _allTransactions

    fun fetchAllTransactions(preferredCurrency: String) {
        viewModelScope.launch {
            try {
                val transactions = repository.getAllTransactions(preferredCurrency)
                _allTransactions.postValue(transactions)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching all transactions", e)
            }
        }
    }


}
