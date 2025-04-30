package com.example.prog_poe_2025

import Data_Classes.Notification
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).notificationDao()

    val notifications: LiveData<List<Notification>> = dao.getAllNotifications()

    fun insertNotification(notification: Notification) {
        viewModelScope.launch {
            dao.insertNotification(notification)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            dao.clearNotifications()
        }
    }
}