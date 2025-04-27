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
            VbBudget(
                "Monthly Grocery Budget", 5000f, mapOf(
                    "Groceries" to 1500f,
                    "Transport" to 800f,
                    "Entertainment" to 1200f,
                    "Other" to 3000f
                )
            ),
            VbBudget(
                "Yearly Holiday Fund", 20000f, mapOf(
                    "Flights" to 10000f,
                    "Accommodation" to 6000f,
                    "Transport" to 3000f
                )
            )
        )





        recyclerView.adapter = BudgetAdapter(sampleBudgets)
    }
}
