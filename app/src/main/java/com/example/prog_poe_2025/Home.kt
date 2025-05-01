package com.example.prog_poe_2025

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Home : AppCompatActivity() {

    private lateinit var txtAccAmount: TextView
    private lateinit var txtSpAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtAccAmount = findViewById(R.id.txtAccAmount)
        txtSpAmount = findViewById(R.id.txtSpAmount)

        setupBottomNavigation()
        fetchTotals() // ✅ Load income & spent totals on startup
        fetchLatestTransactions() // ✅ Load latest transactions for table
        // ✅ Set up button navigation
        findViewById<Button>(R.id.btnCreateBudget).setOnClickListener {
            startActivity(Intent(this@Home, CreateBudget::class.java))
        }

        findViewById<Button>(R.id.btnGenerateReport).setOnClickListener {
            startActivity(Intent(this@Home, GenReport::class.java))
        }

    }

    // ✅ Fetch total accumulated income & spent amount
    private fun fetchTotals() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val userId = SessionManager.getUserId(applicationContext)

            val totalIncome = db.incomeDao().getTotalIncome(userId) ?: 0.0
            val totalSpent = db.expensesDao().getTotalExpenses(userId) ?: 0.0

            Log.d("DEBUG", "Total Income: $totalIncome | Total Spent: $totalSpent") // ✅ Debug log

            withContext(Dispatchers.Main) {
                txtAccAmount.text = "R${String.format("%.2f", totalIncome)}"
                txtSpAmount.text = "R${String.format("%.2f", totalSpent)}"
            }
        }
    }

    private fun fetchLatestTransactions() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val userId = SessionManager.getUserId(applicationContext)

            val latestIncomes = db.incomeDao().getLatestIncomes(userId) ?: emptyList()
            val latestExpenses = db.expensesDao().getLatestExpenses(userId) ?: emptyList()

            val combinedTransactions = (latestIncomes + latestExpenses)
                .sortedByDescending { it.date }
                .take(3) // ✅ Ensures only the latest 3 are taken

            Log.d("DEBUG", "Latest 3 Transactions: $combinedTransactions")

            withContext(Dispatchers.Main) {
                updateTable(combinedTransactions)
            }
        }
    }

    // ✅ Populate the Recent Transactions Table
    private fun updateTable(transactions: List<spTransaction>) {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        tableLayout.removeAllViews() // ✅ Clears previous rows

        if (transactions.isEmpty()) {
            val placeholderRow = TableRow(this)
            val placeholderText = TextView(this).apply {
                text = "No Recent Transactions"
                setPadding(16, 16, 16, 16)
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.GRAY)
            }
            placeholderRow.addView(placeholderText)
            tableLayout.addView(placeholderRow)
            return
        }

        val headerRow = TableRow(this)
        val headers = listOf("Type", "Amount", "Date", "Category")

        for (headerText in headers) {
            val textView = TextView(this).apply {
                text = headerText
                setPadding(16, 16, 16, 16)
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        for (transaction in transactions) { // ✅ Loops properly through 3 items
            val row = TableRow(this)

            row.addView(createCell(if (transaction.isExpense) "Expense" else "Income"))
            row.addView(createCell("R${String.format("%.2f", transaction.amount.toFloat())}"))
            row.addView(createCell(formatDate(transaction.date)))
            row.addView(createCell(transaction.category))

            tableLayout.addView(row)
        }
    }
    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
    private fun createCell(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(16, 16, 16, 16)
            textSize = 13f
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> {
                    startActivity(Intent(this, LogIncomeExpense::class.java))
                    true
                }
                R.id.nav_home -> true
                R.id.nav_viewBudgets -> {
                    startActivity(Intent(this, ViewBudgets::class.java))
                    true
                }
                R.id.nav_game -> {
                    startActivity(Intent(this, BudgetQuiz::class.java))
                    true
                }
                else -> false
            }
        }
    }
}