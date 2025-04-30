package com.example.prog_poe_2025

import Data_Classes.Notification
import android.content.Context

class NotificationHelper(private val context: Context) {

    private val dao = AppDatabase.getDatabase(context).notificationDao()

    suspend fun logLoginNotification(username: String) {
        dao.insertNotification(Notification(
            title = "Login Successful",
            message = "$username logged in."
        ))
    }

    suspend fun logIncomeNotification(amount: Double, category: String) {
        dao.insertNotification(Notification(
            title = "Income Logged",
            message = "$amount added to $category."
        ))
    }

    suspend fun logExpenseNotification(amount: Double, category: String) {
        dao.insertNotification(Notification(
            title = "Expense Logged",
            message = "$amount spent on $category."
        ))
    }

    suspend fun logCustomNotification(title: String, message: String) {
        dao.insertNotification(Notification(title = title, message = message))
    }
}
