package com.example.prog_poe_2025

import DAOs.NotificationDao
import Data_Classes.Notification
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val notificationDao: NotificationDao) {

    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()

    suspend fun insert(notification: Notification) {
        notificationDao.insertNotification(notification)
    }

}

