package com.example.prog_poe_2025

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.recyclerview.widget.RecyclerView


data class VbBudget(
    val name: String,
    val totalBudget: Float,
    val spentAmounts: Map<String, Float> // Category -> Amount spent
)

class ViewBudgets : AppCompatActivity() {

    private lateinit var budgetsRecyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_budgets)

        // Initialize RecyclerView
        budgetsRecyclerView = findViewById(R.id.budgetsRecyclerView)
        budgetsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Example list (replace with real data later)
        val vbBudgetsList = listOf(
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

        budgetAdapter = BudgetAdapter(vbBudgetsList)
        budgetsRecyclerView.adapter = budgetAdapter

        // Bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_viewBudgets
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> {
                    startActivity(Intent(this, LogIncomeExpense::class.java))
                    true
                }
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_viewBudgets -> true
                R.id.nav_game -> {
                    startActivity(Intent(this, BudgetQuiz::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
