package com.example.prog_poe_2025

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "UserPrefs"
    private const val KEY_USER_ID = "firebase_user_id"
    private const val KEY_BUDGET_ID = "budget_id"

    // Save Firebase User ID to SharedPreferences
    fun saveUserId(context: Context, userId: String) { // Changed userId type to String
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_ID, userId).apply() // Using putString
    }

    // Retrieve Firebase User ID from SharedPreferences
    fun getUserId(context: Context): String? { // Changed return type to String?
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null) // Using getString, default to null
    }

    // Clear Firebase User ID on logout
    fun clearUserId(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    fun saveSelectedBudgetId(context: Context, budgetId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BUDGET_ID, budgetId).apply()
    }

    // Retrieve Selected Budget ID from SharedPreferences (remains Int)
    fun getSelectedBudgetId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BUDGET_ID, null) // Null if no budget selected
    }
    // Clear Selected Budget ID when needed
    fun clearSelectedBudgetId(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_BUDGET_ID).apply()
    }
}