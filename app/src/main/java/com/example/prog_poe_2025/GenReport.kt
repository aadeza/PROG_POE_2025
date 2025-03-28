package com.example.prog_poe_2025

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate


class GenReport : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genreport)

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.budgetRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data for testing
        val sampleBudgets = listOf(
            Budget("Groceries", "Groceries for March", 5000.0, 2500.0,
                LocalDate.parse("2025-03-01"), LocalDate.parse("2025-03-31"),
                listOf("Cereal", "Snacks", "Take-Aways"), 50, LocalDate.now()),
            Budget("Entertainment", "Movies and events for March", 1000.0, 800.0,
                LocalDate.parse("2025-03-01"), LocalDate.parse("2025-03-31"),
                listOf("Movies", "Market", "Club"), 80, LocalDate.now())
        )




        recyclerView.adapter = BudgetAdapter(sampleBudgets)
    }
}
