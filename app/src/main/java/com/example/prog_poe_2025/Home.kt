package com.example.prog_poe_2025

import android.R.attr.text
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tableLayout = findViewById(R.id.tableLayout)

        val database = AppDatabase.getDatabase(applicationContext)
        val incomeDao = database.incomeDao()
        val expenseDao = database.expensesDao()
        val repository = HomeRepository(incomeDao, expenseDao)
        val factory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        viewModel.latestTransactions.observe(this) { transactions ->
            displayTransactions(transactions)
        }


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> {
                    startActivity(Intent(this, LogIncomeExpense::class.java))
                    true
                }

                R.id.nav_home -> {
                    true
                }

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


        val toGenReport = findViewById<Button>(R.id.btnGenerateReport)
        toGenReport.setOnClickListener {
            val intent = Intent(this@Home, GenReport::class.java)
            startActivity(intent)
        }

        val toCreateBudget = findViewById<Button>(R.id.btnCreateBudget)
        toCreateBudget.setOnClickListener() {
            val intent = Intent(this, CreateBudget::class.java)
            startActivity(intent)
        }

        val settings = findViewById<ImageButton>(R.id.btnSettings)

        settings.setOnClickListener{
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }

    fun displayTransactions(transactions: List<TransactionItem>) {
        tableLayout.removeAllViews()

        var headerRow = TableRow(this)
        val headers =
            listOf("Entry Type", "Amount", "Date", "Category", "Transaction Type")
        for (headerText in headers) {
            val textView = TextView(this).apply {
                text = headerText
                setPadding(16, 16, 16, 16)
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)

            }
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        for (transaction in transactions) {
            val row = TableRow(this)

            when (transaction) {
                is TransactionItem.IncomeItem -> {
                    row.addView(createCell("Income"))
                    row.addView(createCell(transaction.income.amount.toString()))
                    row.addView(createCell(formatDate(transaction.income.date)))
                    row.addView(createCell(transaction.income.category))
                    row.addView(createCell(transaction.income.transaction_type))
                }

                is TransactionItem.ExpenseItem -> {
                    row.addView(createCell("Expense"))
                    row.addView(createCell(transaction.expense.amount.toString()))
                    row.addView(createCell(formatDate(transaction.expense.date)))
                    row.addView(createCell(transaction.expense.category))
                    row.addView(createCell(transaction.expense.transaction_type))
                }
            }
            tableLayout.addView(row)
        }
    }

    fun createCell(text: String): TextView{
        return TextView(this).apply{
            this.text = text
            setPadding(16, 16, 16, 16)
            textSize = 13f
        }
    }

    fun formatDate(millis: Long): String{
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}

