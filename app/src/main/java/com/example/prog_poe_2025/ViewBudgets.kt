package com.example.prog_poe_2025
import Data_Classes.Category
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prog_poe_2025.databinding.ActivityViewBudgetsBinding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

// Data class for the budget model
data class VbBudget(
    val id: Int,
    val name: String,
    val maxMonthGoal: Long,
    val spentAmounts: Map<Category, Float>,
   val totalSpent: Float,
    val remainingAmount: Float // Added this
)
class ViewBudgets : AppCompatActivity() {

    private lateinit var binding: ActivityViewBudgetsBinding
    private lateinit var budgetsRecyclerView: RecyclerView
    private lateinit var budgetAdapter: BudgetAdapter

    // Database initialization
    private val db by lazy { AppDatabase.getDatabase(this) }
    private val budgetDao by lazy { db.budgetDao() }
    private val expensesDao by lazy { db.expensesDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityViewBudgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        setupRecyclerView()

        // Fetch initial budget data
        fetchBudgets()

        // Set up Bottom Navigation
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        fetchBudgets() // Auto-refresh budget list when returning
    }

    private fun setupRecyclerView() {
        budgetsRecyclerView = binding.budgetsRecyclerView
        budgetsRecyclerView.layoutManager = LinearLayoutManager(this)
        budgetAdapter = BudgetAdapter(emptyList()) // Initialize with empty list
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
                R.id.nav_viewBudgets -> true // ✅ Keeps user on current screen
                R.id.nav_game -> {
                    startActivity(Intent(this, BudgetQuiz::class.java))
                    true
                }
                else -> false
            }
        }
    }

    public fun fetchBudgets() {
        val userId = SessionManager.getUserId(applicationContext)
        val defaultStartTime = 0L // Fetch all transactions

        lifecycleScope.launch(Dispatchers.IO) {
            val budgets = budgetDao.getBudgetsForUser(userId)
            Log.d("DEBUG", "Fetched budgets: $budgets")

            // ✅ Process categories and spending separately for clarity
            val vbBudgetsList = budgets.map { budget ->
                val budgetWithCategories = budgetDao.getBudgetWithCategories(budget.id)

                Log.d("DEBUG", "Budget ID: ${budget.id}, Categories: ${budgetWithCategories.categories.map { it.name }}")

                // ✅ Raw expense values (used for home screen & reports)
                val spentAmountsRaw = budgetWithCategories.categories.associateWith { category ->
                    expensesDao.getTotalSpentInCategory(userId, category.name, budget.id, defaultStartTime) ?: 0f
                }

                // ✅ Adjusted values for the pie chart (expense minus income)
                val spentAmountsForPieChart = budgetWithCategories.categories.associateWith { category ->
                    val totalSpent = spentAmountsRaw[category] ?: 0f
                    val totalIncome = db.incomeDao().getTotalIncomeInCategory(userId, category.name, budget.id, defaultStartTime) ?: 0f
                    maxOf(totalSpent - totalIncome, 0f)
                }

                val totalSpent = spentAmountsRaw.values.sum()
                val remainingAmount = budget.maxMonthGoal - totalSpent

                Log.d("DEBUG", "Budget ID: ${budget.id}, Total Spent: $totalSpent, Remaining: $remainingAmount")

                VbBudget(
                    budget.id,
                    budget.name,
                    budget.maxMonthGoal,
                    spentAmountsRaw, // ✅ Keeps raw expenses intact for home & reports
                    totalSpent,
                    remainingAmount
                )
            }

            // ✅ Switch to Main thread only AFTER processing budgets in IO
            withContext(Dispatchers.Main) {
                if (vbBudgetsList.isEmpty()) {
                    binding.txtNoBudgetsMessage.apply {
                        visibility = View.VISIBLE
                        text = "No budgets created yet. Add a new budget to start tracking!"
                    }
                    budgetAdapter.updateBudgets(emptyList())
                } else {
                    binding.txtNoBudgetsMessage.visibility = View.GONE
                    budgetsRecyclerView.visibility = View.VISIBLE
                    budgetAdapter.updateBudgets(vbBudgetsList)
                    budgetsRecyclerView.adapter?.notifyDataSetChanged()
                    Log.d("DEBUG", "UI Updated - Budgets refreshed successfully")
                }
            }
        }
    }
}