package com.example.prog_poe_2025

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import Data_Classes.Notification
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow

class NotificationViewModel(application: Application,private val repository: NotificationRepository) : AndroidViewModel(application) {

    val allNotifications: Flow<List<Notification>> = repository.allNotifications

    fun insert(notification: Notification) {
        viewModelScope.launch {
            repository.insert(notification)
        }
    }

}