package com.example.prog_poe_2025

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var txtAccAmount: TextView
    private lateinit var txtSpAmount: TextView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        findViewById<Button>(R.id.btnCreateBudget).setOnClickListener {
            startActivity(Intent(this@Home, CreateBudget::class.java))
        }

        findViewById<Button>(R.id.btnGenerateReport).setOnClickListener {
            startActivity(Intent(this@Home, GenReport::class.java))
        }
    }

    // ✅ Fetch total accumulated income & spent amount from Firestore
    private fun fetchTotals() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = SessionManager.getUserId(applicationContext)

            try {
                if (userId.isNullOrEmpty()) {
                    Log.e("Home", "Error: User ID is null or empty")
                    return@launch
                }

                val incomeSnapshot = firestore.collection("incomes")
                    .whereEqualTo("userId", userId) // ✅ Fetch directly from "incomes"
                    .get()
                    .await()

                val expenseSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userId) // ✅ Fetch directly from "expenses"
                    .get()
                    .await()

                val totalIncome = incomeSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val totalSpent = expenseSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

                Log.d("DEBUG", "Total Income: $totalIncome | Total Spent: $totalSpent")

                withContext(Dispatchers.Main) {
                    txtAccAmount.text = "R${String.format("%.2f", totalIncome)}"
                    txtSpAmount.text = "R${String.format("%.2f", totalSpent)}"
                }
            } catch (e: Exception) {
                Log.e("Home", "Error fetching totals", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Failed to load totals.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchLatestTransactions() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = SessionManager.getUserId(applicationContext)
            if (userId.isNullOrEmpty()) {
                Log.e("Home", "Error: User ID is null or empty")
                return@launch
            }

            try {
                Log.d("DEBUG", "Fetching latest transactions for user: $userId")

                val incomesSnapshot = firestore.collection("incomes")
                    .whereEqualTo("user_id", userId) // ✅ Fixed field name
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .await()

                val expensesSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userId) // ✅ Fixed field name
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .await()

                Log.d("DEBUG", "Fetched incomes count: ${incomesSnapshot.documents.size}")
                Log.d("DEBUG", "Fetched expenses count: ${expensesSnapshot.documents.size}")

                val latestTransactions = (incomesSnapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    Income(id, amount, date)
                } + expensesSnapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    Expense(id, amount, date)
                }).sortedByDescending { it.date }

                withContext(Dispatchers.Main) {
                    updateTable(latestTransactions)
                }

            } catch (e: Exception) {
                Log.e("Home", "Error fetching latest transactions: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Failed to load transactions.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ✅ Populate the Recent Transactions Table
    private fun updateTable(transactions: List<Transaction>) {
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
        val headers = listOf("Type", "Amount", "Date")

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
            row.addView(createCell(if (transaction.isExpense) "Expense" else "Income"))
            row.addView(createCell("R${String.format("%.2f", transaction.amount)}"))
            row.addView(createCell(formatDate(transaction.date)))

            tableLayout.addView(row)
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
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
/*References List
Svaghasiya, 2023. Using ViewModel in Android With Kotlin, 18 September 2023. [Online]. Available at:
https://medium.com/@ssvaghasiya61/using-viewmodel-in-android-with-kotlin-16ca735c644f [Accessed 25 April 2025].

TutorialsPoint, 2025. Android- Date Picker, n.d. [Online]. Available at:
https://www.tutorialspoint.com/android/android_datepicker_control.htm [Accessed 21 April 2025].
*
* */