package com.example.prog_poe_2025

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurrencyViewModel(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    suspend fun saveCurrencyCode(currencyCode: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString("preferred_currency", currencyCode).apply()
        }
    }

    suspend fun getPreferredCurrency(): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString("preferred_currency", "USD") // Default to USD
        }
    }
}
