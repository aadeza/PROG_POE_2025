package com.example.prog_poe_2025
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
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
        fetchTotals()
        fetchLatestTransactions()

        findViewById<Button>(R.id.btnCreateBudget).setOnClickListener {
            startActivity(Intent(this@Home, CreateBudget::class.java))
        }

        findViewById<Button>(R.id.btnGenerateReport).setOnClickListener {
            startActivity(Intent(this@Home, GenReport::class.java))
        }

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this@Home, Settings::class.java))
        }

        findViewById<ImageButton>(R.id.btnNotification).setOnClickListener {
            showNotificationPopup()
        }
    }
    /**
     * Source: https://firebase.google.com/docs/firestore/query-data/get-data
     * Author: Firebase Documentation (Google)
     * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
     * Adapted by: Ade-Eza Silongo for Pennywise
     * Purpose: Fetches user data from Firestore and updates the UI accordingly
     * Modifications:
     * - Added Kotlin coroutine support for asynchronous calls
     * - Custom error handling and logging added
     */
    private fun showNotificationPopup() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val budgetEnabled = prefs.getBoolean("budget_notifications_enabled", false)
        val transactionEnabled = prefs.getBoolean("transaction_notifications_enabled", false)
        val frequency = prefs.getString("notification_frequency", "15_min") ?: "15_min"

        // 🔹 Retrieve notifications from separate storage
        val notificationPrefs = getSharedPreferences("notification_history", Context.MODE_PRIVATE)
        val notificationHistory = notificationPrefs.getStringSet("notifications", emptySet())?.toList()?.sortedByDescending {
            it.split(" | ").getOrNull(1)?.toLongOrNull() ?: 0L
        } ?: emptyList()

        // 🔹 Extract latest notification details
        val lastNotificationEntry = notificationHistory.lastOrNull() ?: "No recent notifications"
        val lastNotificationParts = lastNotificationEntry.split(" | ")
        val lastNotificationText = lastNotificationParts.getOrNull(0) ?: "Unknown notification"
        val lastNotificationTimestamp = lastNotificationParts.getOrNull(1)?.toLongOrNull()

        // 🔹 Format timestamp for better readability
        val lastNotificationFormattedTime = lastNotificationTimestamp?.let { formatDateTime(it) } ?: "Not available"
        val latestNotificationDisplay = "$lastNotificationText\nLast notified: $lastNotificationFormattedTime"

        // 🔹 Prepare UI elements
        val dialogView = layoutInflater.inflate(R.layout.notification_popup, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()
        dialog.show()

        val switchBudgetNotify = dialogView.findViewById<Switch>(R.id.switchBudgetNotify)
        val switchTransactionNotify = dialogView.findViewById<Switch>(R.id.switchTransactionNotify)
        val txtFrequency = dialogView.findViewById<TextView>(R.id.txtFrequency)
        val txtLatestNotifications = dialogView.findViewById<TextView>(R.id.txtLatestNotifications)
        val btnClosePopup = dialogView.findViewById<Button>(R.id.btnClosePopup)

        // 🔹 Apply values to UI
        switchBudgetNotify.isChecked = budgetEnabled
        switchTransactionNotify.isChecked = transactionEnabled
        txtFrequency.text = "Selected Frequency: $frequency"
        txtLatestNotifications.text = latestNotificationDisplay

        // 🔹 Handle notification preference changes
        switchBudgetNotify.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("budget_notifications_enabled", isChecked).apply()
        }

        switchTransactionNotify.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("transaction_notifications_enabled", isChecked).apply()
        }

        btnClosePopup.setOnClickListener {
            dialog.dismiss()
        }
    }

    // ✅ Helper Function to Format Timestamp
    private fun formatDateTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }



    private fun getFrequencyMillis(frequencyKey: String): Long {
        return when (frequencyKey) {
            "15_min" -> 15 * 60 * 1000L  // 🔹 15 minutes (lowest interval)
            "1_hr" -> 1 * 60 * 60 * 1000L  // 🔹 1 hour
            "6_hr" -> 6 * 60 * 60 * 1000L  // 🔹 6 hours
            "12_hr" -> 12 * 60 * 60 * 1000L // 🔹 12 hours
            else -> 1 * 60 * 60 * 1000L  // 🔹 Default to 1 hour if invalid key
        }
    }


    private fun fetchTotals() {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = SessionManager.getUserId(applicationContext)
            if (userId.isNullOrEmpty()) {
                Log.e("Home", "Error: User ID is null or empty")
                return@launch
            }

            try {
                val incomeSnapshot = firestore.collection("incomes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val expenseSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val totalIncome = incomeSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                val totalSpent = expenseSnapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

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
                val incomesSnapshot = firestore.collection("incomes")
                    .whereEqualTo("userId", userId)

                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .await()

                val expensesSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userId)

                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)
                    .get()
                    .await()

                val latestTransactions = (incomesSnapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    val categoryId = doc.getString("categoryId") // will be null if not set
                    Income(
                        id = id,
                        amount = amount,
                        date = date,
                        categoryId = categoryId
                    )
                } + expensesSnapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    val categoryId = doc.getString("categoryId")
                    Expense(
                        id = id,
                        amount = amount,
                        date = date,
                        categoryId = categoryId
                    )
                }).sortedByDescending { it.date }

                withContext(Dispatchers.Main) {
                    updateTable(latestTransactions)
                }
            } catch (e: Exception) {
                Log.e("Home", "Error fetching latest transactions", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Failed to load transactions.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTable(transactions: List<Transaction>) {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        tableLayout.removeAllViews()

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