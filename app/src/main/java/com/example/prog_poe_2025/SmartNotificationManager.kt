package com.example.prog_poe_2025

import Data_Classes.Expenses
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import Data_Classes.Notification
import Data_Classes.Income
import android.content.Context

object SmartNotificationManager {
    suspend fun createIncomeLoggedNotification(context: Context, income: Income) {
        val message = "Income logged: ${income.amount} in ${income.category}"
        showAndSave(context, "Income Logged", message)
    }

    suspend fun createExpenseLoggedNotification(context: Context, expense: Expenses) {
        val message = "Expense logged: ${expense.amount} in ${expense.category}"
        showAndSave(context, "Expense Logged", message)
    }

    private suspend fun showAndSave(context: Context, title: String, message: String) {
        // Show the system notification if you want (not required)
        // Notification logic...

        val notification = Notification(
            title = title,
            message = message,
            timestamp = System.currentTimeMillis()
        )
        val dao = AppDatabase.getDatabase(context).notificationDao()
        dao.insertNotification(notification)
    }
}



