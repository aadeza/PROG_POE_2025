package com.example.prog_poe_2025

import Data_Classes.Notification
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.prog_poe_2025.databinding.CardNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var viewModel: NotificationViewModel
    private lateinit var notificationContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Initialize the notification container (the LinearLayout where notifications will appear)
        notificationContainer = findViewById(R.id.notificationContainer)

        // Initialize the ViewModel to observe the notifications
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        // Observe the LiveData from the ViewModel
        viewModel.notifications.observe(this, Observer { notifications ->
            displayNotifications(notifications)
        })
    }

    private fun displayNotifications(notifications: List<Notification>) {

        notificationContainer.removeAllViews()

        for (notification in notifications) {
            val cardBinding = CardNotificationBinding.inflate(LayoutInflater.from(this))

            // Bind notification data to the CardView
            cardBinding.txtTitle.text = notification.title
            cardBinding.txtMessage.text = notification.message
            cardBinding.txtTimestamp.text = DateFormat.format("dd MMM yyyy, hh:mm a", notification.timestamp)

            // Add the CardView to the container
            notificationContainer.addView(cardBinding.root)
        }
    }
}