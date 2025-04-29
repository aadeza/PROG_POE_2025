package com.example.prog_poe_2025

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class NotificationActivity : AppCompatActivity() {

    private lateinit var viewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val dao = AppDatabase.getDatabase(this).notificationDao()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ Observe LiveData directly from the lifecycle owner (this)
        dao.getAllNotifications().observe(this) { notifications ->
            val adapter = NotificationAdapter(notifications)
            recyclerView.adapter = adapter
        }
    }
}

