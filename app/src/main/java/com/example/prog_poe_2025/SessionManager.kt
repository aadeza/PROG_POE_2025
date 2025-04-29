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



        fun saveUsername(context: Context, username: String) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString("username", username)
            editor.apply()
        }

        fun getUsername(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString("username", null)
        }

        fun saveEmail(context: Context, email: String) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString("email", email)
            editor.apply()
        }

        fun getEmail(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString("email", null)
        }

        fun saveProfileImageUri(context: Context, uri: String) {
            val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            editor.putString("profile_image_uri", uri)
            editor.apply()
        }

        fun getProfileImageUri(context: Context): String? {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString("profile_image_uri", null)
        }

        fun clearSession(context: Context) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        }


}
