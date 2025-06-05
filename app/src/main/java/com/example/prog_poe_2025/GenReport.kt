package com.example.prog_poe_2025

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ActivityGenreportBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Report(
    val id: String, // Firestore doc ID
    val budgetName: String,
    val maxAmount: Long,
    val startDate: Long,
    val endDate: Long,
    val highestExpense: Float,
    val highestIncome: Float,
    val categories: List<String>,
    val isExpense: Boolean
)

interface Transaction {
    val id: String
    val amount: Double
    val date: Long
    val isExpense: Boolean
    val categoryId: String?
    val imageUrl: String? // ✅ Add this property
}

data class Expense(
    override val id: String,
    override val amount: Double,
    override val date: Long,
    override val isExpense: Boolean = true,
    override val categoryId: String?,
    override val imageUrl: String? = null // ✅ Implement it
) : Transaction

data class Income(
    override val id: String,
    override val amount: Double,
    override val date: Long,
    override val isExpense: Boolean = false,
    override val categoryId: String?,
    override val imageUrl: String? = null // ✅ Implement it
) : Transaction


class GenReport : AppCompatActivity() {

    private lateinit var binding: ActivityGenreportBinding
    private lateinit var reportRecyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenreportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchUserInfo()
        generateReport()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_transaction -> {
                    startActivity(Intent(this, LogIncomeExpense::class.java))
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

        val txtAllTransactions = findViewById<TextView>(R.id.txtAllTransactions)
        txtAllTransactions.setOnClickListener {
            showTransactionsPopup()
        }
    }

    override fun onResume() {
        super.onResume()
        generateReport()
    }

    private fun setupRecyclerView() {
        reportRecyclerView = binding.budgetRecyclerView
        reportRecyclerView.layoutManager = LinearLayoutManager(this)
        reportAdapter = ReportAdapter(emptyList())
        reportRecyclerView.adapter = reportAdapter
    }

    private fun fetchUserInfo() {
        val userID = SessionManager.getUserId(applicationContext)
        if (userID != null) {
            firestore.collection("users").document(userID)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        binding.txtName.text = "Name: ${doc.getString("name") ?: "N/A"}"
                        binding.txtSurname.text = "Surname: ${doc.getString("surname") ?: "N/A"}"
                        binding.txtUsername.text = "Username: ${doc.getString("email") ?: "N/A"}"
                        binding.txtStreakProgress.text = "Current streak progress: N/A"
                        binding.txtQuizScore.text = "Average quiz score: N/A"
                    } else {
                        Toast.makeText(this, "User info not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user info.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun generateReport() {
        val userID = SessionManager.getUserId(applicationContext)
        if (userID == null) {
            Toast.makeText(applicationContext, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 🔹 Fetch Budgets Linked to User
                val budgetsSnapshot = firestore.collection("budgets")
                    .whereEqualTo("user_id", userID)
                    .get()
                    .await()

                withContext(Dispatchers.Main) {
                    if (budgetsSnapshot.isEmpty) {
                        binding.txtBudgetsCreated.text = "Budgets created: 0"
                        Toast.makeText(
                            applicationContext,
                            "No budgets available to generate a report.",
                            Toast.LENGTH_SHORT
                        ).show()
                        reportAdapter.updateBudgets(emptyList())
                        return@withContext
                    }

                    binding.txtBudgetsCreated.text = "Budgets created: ${budgetsSnapshot.size()}"
                }

                // 🔹 Create Budget Reports
                val reports = budgetsSnapshot.documents.map { budgetDoc ->
                    val budgetId = budgetDoc.id
                    val budgetName = budgetDoc.getString("name") ?: "Unknown"
                    val maxAmount = budgetDoc.getLong("maxMonthGoal") ?: 0L
                    val startDate = budgetDoc.getLong("startDate") ?: 0L
                    val endDate = budgetDoc.getLong("endDate") ?: 0L

                    // 🔹 Fetch Highest Expense for This Budget
                    val highestExpenseSnapshot = firestore.collection("expenses")
                        .whereEqualTo("budgetId", budgetId)
                        .orderBy("amount", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    val highestExpense =
                        highestExpenseSnapshot.documents.firstOrNull()?.getDouble("amount")
                            ?.toFloat() ?: 0f

                    // 🔹 Fetch Highest Income for This Budget
                    val highestIncomeSnapshot = firestore.collection("incomes")
                        .whereEqualTo("budgetId", budgetId)
                        .orderBy("amount", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    val highestIncome =
                        highestIncomeSnapshot.documents.firstOrNull()?.getDouble("amount")
                            ?.toFloat() ?: 0f

                    // 🔹 Fetch Categories for This Budget (Correcting ID -> Name)
                    val categoryIds = budgetDoc.get("categories") as? List<String> ?: emptyList()
                    val categoryNames = mutableListOf<String>()

                    for (categoryId in categoryIds) {
                        val categoryDoc =
                            firestore.collection("categories").document(categoryId).get().await()
                        categoryNames.add(categoryDoc.getString("name") ?: "Unknown")
                    }

                    Report(
                        id = budgetId,
                        budgetName = budgetName,
                        maxAmount = maxAmount,
                        startDate = startDate,
                        endDate = endDate,
                        highestExpense = highestExpense,
                        highestIncome = highestIncome,
                        categories = categoryNames, // ✅ Now correctly shows category names
                        isExpense = highestExpense > highestIncome
                    )
                }

                withContext(Dispatchers.Main) {
                    reportAdapter.updateBudgets(reports)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Failed to generate report: ${e.message}")
                    Toast.makeText(
                        applicationContext,
                        "Failed to generate report: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
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
     * - Integrated with LiveData to observe data changes
     * - Custom error handling and logging added
     */

    private fun showTransactionsPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.transaction_popup, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()
        dialog.show()

        val recyclerTransactions = dialogView.findViewById<RecyclerView>(R.id.recyclerTransactions)
        val btnClosePopup = dialogView.findViewById<Button>(R.id.btnClosePopup)

        recyclerTransactions.layoutManager = LinearLayoutManager(this)

        val userID = SessionManager.getUserId(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("TransactionPopup", "Fetching transactions for userId: $userID")

                // 🔹 Fetch Expenses
                val expensesSnapshot = firestore.collection("expenses")
                    .whereEqualTo("userId", userID)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val expenses = expensesSnapshot.documents.map { doc ->
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    Log.d("TransactionPopup", "Fetched Expense ID: ${doc.id}, Image URL: $imageUrl")

                    Expense(
                        id = doc.id,
                        amount = doc.getDouble("amount") ?: 0.0,
                        date = doc.getLong("date") ?: 0L,
                        categoryId = doc.getString("categoryId"),
                        imageUrl = imageUrl
                    )
                }

                // 🔹 Fetch Incomes
                val incomesSnapshot = firestore.collection("incomes")
                    .whereEqualTo("userId", userID)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val incomes = incomesSnapshot.documents.map { doc ->
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    Log.d("TransactionPopup", "Fetched Income ID: ${doc.id}, Image URL: $imageUrl")

                    Income(
                        id = doc.id,
                        amount = doc.getDouble("amount") ?: 0.0,
                        date = doc.getLong("date") ?: 0L,
                        categoryId = doc.getString("categoryId"),
                        imageUrl = imageUrl
                    )
                }

                val allTransactions = (expenses + incomes).sortedByDescending { it.date }

                // 🔹 Fetch all category names once
                val categoryMap = fetchCategoryNames()

                withContext(Dispatchers.Main) {
                    val adapter = TransactionAdapter(allTransactions, categoryMap)
                    recyclerTransactions.adapter = adapter

                    // 🔹 Ensure UI updates
                    adapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("TransactionPopup", "Error fetching transactions: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@GenReport,
                        "Failed to load transactions: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        btnClosePopup.setOnClickListener { dialog.dismiss() }
    }


    private suspend fun fetchCategoryNames(): Map<String, String> {
        val categorySnapshot = firestore.collection("categories").get().await()
        return categorySnapshot.documents.associate {
            it.id to (it.getString("name") ?: "Unknown")
        }
    }
}

