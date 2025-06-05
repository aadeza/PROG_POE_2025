package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ActivityViewBudgetsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

data class VbBudget(
    val id: String = "",
    val name: String = "",
    val maxMonthGoal: Long = 0L,
    val minMonthGoal: Long = 0L,
    val spentAmounts: Map<Category, Float>,
    val totalSpent: Float,
    val remainingAmount: Float,
    val categories: List<Category>
)

class ViewBudgets : AppCompatActivity() {

    private lateinit var binding: ActivityViewBudgetsBinding
    private lateinit var budgetsRecyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBudgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // ✅ Default to last 24 hours when the screen opens
        val defaultFilterHours = binding.spinnerTimeFilter.selectedItemPosition.let {
            when (it) {
                0 -> 1  // Last 1 Hour
                1 -> 6  // Last 6 Hours
                2 -> 24 // Last 24 Hours (1 Day)
                3 -> -1 // All Time
                else -> 24
            }
        }

        setupRecyclerView(defaultFilterHours)
        fetchBudgets(defaultFilterHours) // ✅ Apply filter immediately on load

        setupRecyclerView(defaultFilterHours)
        fetchBudgets(defaultFilterHours) // ✅ Ensure filter applies on startup

        binding.spinnerTimeFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilterHours = when (position) {
                    0 -> 1  // Last 1 Hour
                    1 -> 6  // Last 6 Hours
                    2 -> 24 // Last 24 Hours (1 Day)
                    3 -> -1 // All Time
                    else -> 24
                }

                setupRecyclerView(selectedFilterHours)
                fetchBudgets(selectedFilterHours) // ✅ Apply filter dynamically
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    override fun onResume() {
        super.onResume()

        val selectedFilterHours = binding.spinnerTimeFilter.selectedItemPosition.let {
            when (it) {
                0 -> 1  // Last 1 Hour
                1 -> 6  // Last 6 Hours
                2 -> 24 // Last 24 Hours (1 Day)
                3 -> -1 // All Time
                else -> 24 // Default to 24 hours
            }
        }

        fetchBudgets(selectedFilterHours) // ✅ Use last selected filter
    }

    private fun setupRecyclerView(filterHours: Int) { // ✅ Add parameter
        budgetsRecyclerView = binding.budgetsRecyclerView
        budgetsRecyclerView.layoutManager = LinearLayoutManager(this)

        budgetAdapter = BudgetAdapter(emptyList(), filterHours) // ✅ Pass filterHours
        budgetsRecyclerView.adapter = budgetAdapter
    }
    private fun setupBottomNavigation() {
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

    /**
     * Source: https://firebase.google.com/docs/firestore/query-data/get-data
     * Author: Firebase Documentation (Google)
     * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
     * Adapted by: Ade-Eza Silongo, Lusanda Mlotshwa and Reaobaka Ntoagae for Pennywise
     * Purpose: Fetches user data from Firestore and updates the UI accordingly
     * Modifications:
     * - Added Kotlin coroutine support for asynchronous calls
     * - Integrated with LiveData to observe data changes
     * - Custom error handling and logging added
     */
    fun fetchBudgets(timeFilterHours: Int = 24) { // 🔹 Default to last 24 hours if no filter is provided
        val userId = SessionManager.getUserId(applicationContext)
        Log.d("DEBUG", "User ID from SessionManager: $userId")

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val filterStartTime = if (timeFilterHours == -1) 0L else currentTime - (timeFilterHours * 60 * 60 * 1000)
                // 🔹 Use 0L when "All Time" is selected to disable filtering.

                val budgetsSnapshot = db.collection("budgets")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()

                if (budgetsSnapshot.isEmpty) {
                    showNoBudgetsMessage("No budgets created yet. Add a new budget to start tracking!")
                    return@launch
                }

                val budgetDocs = budgetsSnapshot.documents

                // 🔹 Fetch relevant categories
                val allCategoryIds = budgetDocs.flatMap {
                    val rawList = it.get("categories")
                    when (rawList) {
                        is List<*> -> rawList.mapNotNull { id -> id?.toString() }
                        else -> emptyList()
                    }
                }.distinct()

                val catDocs = if (allCategoryIds.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        db.collection("categories")
                            .whereIn(FieldPath.documentId(), allCategoryIds)
                            .get()
                            .await()
                    }
                } else null

                val categoryMap = catDocs?.documents?.associate {
                    val name = it.getString("name") ?: "Unknown"
                    val lastUpdatedTime = it.getLong("lastUpdatedTime") ?: 0L // 🔹 Ensure timestamp exists
                    it.id to Category(it.id, name, lastUpdatedTime = lastUpdatedTime)
                } ?: emptyMap()

                // 🔹 Fetch expenses based on the selected timeframe
                val expensesSnapshot = withContext(Dispatchers.IO) {
                    db.collection("expenses")
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                }

                val expenseSums = expensesSnapshot.documents.mapNotNull { doc ->
                    val budgetId = doc.getString("budgetId")
                    val catId = doc.getString("categoryId")
                    val amt = doc.getDouble("amount")?.toFloat()
                    val expenseDate = doc.getLong("date") ?: 0L // 🔹 Default to 0 if missing

                    if (budgetId != null && catId != null && amt != null && (timeFilterHours == -1 || expenseDate >= filterStartTime))
                        Triple(budgetId, catId, amt) // ✅ Include all transactions for "All Time"
                    else null
                }.groupBy { it.first to it.second }
                    .mapValues { entry -> entry.value.map { it.third }.sum() }

                val vbBudgetsList = budgetDocs.map { bDoc ->
                    val id = bDoc.id
                    val name = bDoc.getString("name") ?: "Unnamed"
                    val maxGoal = bDoc.getLong("maxMonthGoal") ?: 0L
                    val minGoal = bDoc.getLong("minMonthGoal") ?: 0L

                    val categoryIds = (bDoc.get("categories") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val categories = categoryIds.mapNotNull { cid -> categoryMap[cid] }

                    // Remove additional filtering; rely solely on expense date filtering
                    val spentAmounts = categories.associate { category ->
                        val key = id to category.id
                        val amount = expenseSums[key] ?: 0f
                        category to amount
                    }

                    val totalSpent = spentAmounts.values.sum()
                    val remaining = maxGoal.toFloat() - totalSpent

                    VbBudget(
                        id = id,
                        name = name,
                        maxMonthGoal = maxGoal,
                        minMonthGoal = minGoal,
                        spentAmounts = spentAmounts,
                        totalSpent = totalSpent,
                        remainingAmount = remaining,
                        categories = categories
                    )
                }

                withContext(Dispatchers.Main) {
                    if (vbBudgetsList.isEmpty()) {
                        showNoBudgetsMessage("No budgets found.")
                    } else {
                        binding.txtNoBudgetsMessage.visibility = View.GONE
                        budgetsRecyclerView.visibility = View.VISIBLE
                        budgetAdapter.updateBudgets(vbBudgetsList)
                        budgetsRecyclerView.adapter?.notifyDataSetChanged()
                    }
                }

            } catch (e: Exception) {
                Log.e("ViewBudgets", "Error fetching budgets", e)
                Toast.makeText(this@ViewBudgets, "Error fetching budgets", Toast.LENGTH_SHORT).show()
                showNoBudgetsMessage("Error loading budgets.")
            }
        }
    }


    private fun showNoBudgetsMessage(msg: String) {
        binding.txtNoBudgetsMessage.apply {
            visibility = View.VISIBLE
            text = msg
        }
        budgetsRecyclerView.visibility = View.GONE
        budgetAdapter.updateBudgets(emptyList())
    }

    private fun showBudgets(budgets: List<VbBudget>) {
        if (budgets.isEmpty()) {
            showNoBudgetsMessage("No budgets found.")
        } else {
            binding.txtNoBudgetsMessage.visibility = View.GONE
            budgetsRecyclerView.visibility = View.VISIBLE
            budgetAdapter.updateBudgets(budgets)
        }

    }
}