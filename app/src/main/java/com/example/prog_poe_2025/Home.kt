package com.example.prog_poe_2025


import Data_Classes.Notification
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class Home : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var tableLayout: TableLayout
    private lateinit var streakTxt: TextView
    private lateinit var incomeTxt: TextView
    private lateinit var expenseTxt: TextView
    private lateinit var txtNetSavings: TextView

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
        streakTxt = findViewById(R.id.streakText)
        incomeTxt = findViewById(R.id.txtTotalIncome)
        expenseTxt = findViewById(R.id.txtTotalExpense)
        txtNetSavings = findViewById(R.id.txtMyProfit)


        val database = AppDatabase.getDatabase(applicationContext)
        val incomeDao = database.incomeDao()
        val expenseDao = database.expensesDao()
        val streakDao = database.streakDao()

        val repository = HomeRepository(incomeDao, expenseDao)
        val streakRepository = StreakRepository(streakDao)




        val factory = HomeViewModelFactory(application, repository)

        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        notificationViewModel  = ViewModelProvider(this)[NotificationViewModel::class.java]

        viewModel.latestTransactions.observe(this) { transactions ->
            displayTransactions(transactions)
        }

        viewModel.totalIncome.observe(this) { total ->
            val totalIncome = total ?: 0L
            incomeTxt.text = "+ $totalIncome"
        }

        viewModel.setUserId(1)

        viewModel.totalExpenses.observe(this) { total ->
            val totalExpenses = total ?: 0L
            expenseTxt.text = "- $totalExpenses"
        }

       viewModel.netSavings.observe(this){ total ->
       val netSavings = total ?: 0L
       txtNetSavings.text = "$netSavings"
     }



        viewModel.refreshData()

        lifecycleScope.launch {
            val streakCount = streakRepository.getStreakCount()
            streakTxt.text = "$streakCount"
        }

        setupBottomNavigation()

        findViewById<Button>(R.id.btnGenerateReport).setOnClickListener {
            startActivity(Intent(this@Home, GenReport::class.java))
        }

        findViewById<Button>(R.id.btnCreateBudget).setOnClickListener {
            startActivity(Intent(this@Home, CreateBudget::class.java))
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this@Home, Settings::class.java))
        }

        findViewById<ImageButton>(R.id.btnNotification).setOnClickListener {
            startActivity(Intent(this@Home, NotificationActivity::class.java))
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

    override fun onResume() {
        super.onResume()
        val selectedCurrency = getPreferredCurrency(this)
        viewModel.setCurrency(selectedCurrency)
        viewModel.refreshData()
    }


    private fun getPreferredCurrency(context: Context): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        return sharedPreferences.getString("preferred_currency", "USD") ?: "USD"
    }

    private fun displayTransactions(transactions: List<TransactionItem>) {
        tableLayout.removeAllViews()

        val headerRow = TableRow(this)
        val headers = listOf("Entry Type", "Amount", "Date", "Category", "Transaction Type")

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

    private fun createCell(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(16, 16, 16, 16)
            textSize = 13f
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(millis))
    }
}
