package com.example.prog_poe_2025

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "UserPrefs"
    private const val KEY_USER_ID = "user_id"

    // Save User ID to SharedPreferences
    fun saveUserId(context: Context, userId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    // Retrieve User ID from SharedPreferences
    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, -1) // Default value is -1 if no user_id is found
    }

    // Clear User ID on logout
    fun clearUserId(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}
