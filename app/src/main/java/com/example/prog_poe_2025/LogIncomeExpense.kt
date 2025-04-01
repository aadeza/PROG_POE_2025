package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class LogIncomeExpense : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_income_expense)

        // Apply window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up category spinner
        val spinner: Spinner = findViewById(R.id.spnCategory)
        val categories = resources.getStringArray(R.array.category_list)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set up transaction type spinner
        val spinnerTransactType: Spinner = findViewById(R.id.spnTransactType)
        val transactionTypes = resources.getStringArray(R.array.transaction_types)
        val adapter2 = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, transactionTypes)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTransactType.adapter = adapter2

        // Add BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set default selected item
        bottomNavigationView.selectedItemId = R.id.nav_transaction

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> {
                    true // Stay on LogIncomeExpense
                }
                R.id.nav_home -> {
                    // Navigate to Home
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_viewBudgets -> {
                    // Navigate to ViewBudgets
                    startActivity(Intent(this, ViewBudgets::class.java))
                    true
                }
                R.id.nav_game -> {
                    // Navigate to BudgetQuiz
                    startActivity(Intent(this, BudgetQuiz::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
