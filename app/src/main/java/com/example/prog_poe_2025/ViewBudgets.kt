package com.example.prog_poe_2025


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prog_poe_2025.databinding.ActivityViewBudgetsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class VbBudget(
    val id: String,  // Firestore doc ID is a String
    val name: String,
    val maxMonthGoal: Long,
    val spentAmounts: Map<Category, Float>,
    val totalSpent: Float,
    val remainingAmount: Float,
    val categories: List<Category>
)

class ViewBudgets : AppCompatActivity() {

    private lateinit var binding: ActivityViewBudgetsBinding
    private lateinit var budgetsRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewBudgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchBudgets()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        fetchBudgets()
    }

    private fun setupRecyclerView() {
        budgetsRecyclerView = binding.budgetsRecyclerView
        budgetsRecyclerView.layoutManager = LinearLayoutManager(this)
        budgetAdapter = BudgetAdapter(emptyList())
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

    fun fetchBudgets() {
        val userId = SessionManager.getUserId(applicationContext)
        val defaultStartTime = 0L // For "All" time range

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val budgetsSnapshot = db.collection("budgets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val vbBudgetsList = budgetsSnapshot.documents.map { budgetDoc ->

                    val budgetId = budgetDoc.id
                    val budgetName = budgetDoc.getString("name") ?: "Unnamed"
                    val maxMonthGoal = budgetDoc.getLong("maxMonthGoal") ?: 0L

                    val categoriesListData = budgetDoc.get("categories") as? List<Map<String, Any>> ?: emptyList()
                    val categories = categoriesListData.map { catMap ->
                        val catName = catMap["name"] as? String ?: "Unknown"
                        Category(catName)
                    }

                    val spentAmountsDeferred = categories.map { category ->
                        async {
                            val expensesSnapshot = db.collection("expenses")
                                .whereEqualTo("userId", userId)
                                .whereEqualTo("budgetId", budgetId)
                                .whereEqualTo("category", category.name)
                                .whereGreaterThan("timestamp", defaultStartTime)
                                .get()
                                .await()

                            val totalSpentInCat = expensesSnapshot.documents.sumOf {
                                (it.getDouble("amount") ?: 0.0)
                            }.toFloat()

                            category to totalSpentInCat
                        }
                    }

                    val spentAmountsList = spentAmountsDeferred.awaitAll()
                    val spentAmountsMap = spentAmountsList.toMap()

                    val totalSpent = spentAmountsMap.values.sum()
                    val remainingAmount = maxMonthGoal - totalSpent

                    VbBudget(
                        id = budgetId,
                        name = budgetName,
                        maxMonthGoal = maxMonthGoal,
                        spentAmounts = spentAmountsMap,
                        totalSpent = totalSpent,
                        remainingAmount = remainingAmount,
                        categories = categories
                    )
                }

                withContext(Dispatchers.Main) {
                    if (vbBudgetsList.isEmpty()) {
                        binding.txtNoBudgetsMessage.visibility = View.VISIBLE
                        binding.txtNoBudgetsMessage.text = "No budgets created yet. Add a new budget to start tracking!"
                        budgetsRecyclerView.visibility = View.GONE
                        budgetAdapter.updateBudgets(emptyList())
                    } else {
                        binding.txtNoBudgetsMessage.visibility = View.GONE
                        budgetsRecyclerView.visibility = View.VISIBLE
                        budgetAdapter.updateBudgets(vbBudgetsList)
                        budgetsRecyclerView.adapter?.notifyDataSetChanged()
                        Log.d("DEBUG", "UI Updated - Budgets refreshed successfully")
                    }
                }

            } catch (e: Exception) {
                Log.e("ViewBudgets", "Error fetching budgets: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.txtNoBudgetsMessage.visibility = View.VISIBLE
                    binding.txtNoBudgetsMessage.text = "Failed to load budgets."
                    budgetsRecyclerView.visibility = View.GONE
                }
            }
        }
    }
}

// (W3Schools,2025)

/*
Reference List:
W3Schools. 2025. Kotlin Tutorial, n.d.[Online]. Available at:
https://www.w3schools.com/kotlin/index.php  [Accessed 24 April 2025].
 */
