package com.example.prog_poe_2025

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "UserPrefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_BUDGET_ID = "budget_id" // ✅ Added Budget ID key

    // ✅ Save User ID to SharedPreferences
    fun saveUserId(context: Context, userId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    // ✅ Retrieve User ID from SharedPreferences
    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, -1) // Default value is -1 if no user_id is found
    }

    // ✅ Clear User ID on logout
    fun clearUserId(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    // ✅ Save Selected Budget ID to SharedPreferences
    fun saveSelectedBudgetId(context: Context, budgetId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_BUDGET_ID, budgetId).apply()
    }

    // ✅ Retrieve Selected Budget ID from SharedPreferences
    fun getSelectedBudgetId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BUDGET_ID, -1) // Default value is -1 if no budget_id is found
    }

    // ✅ Clear Selected Budget ID when needed
    fun clearSelectedBudgetId(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_BUDGET_ID).apply()
    }
}