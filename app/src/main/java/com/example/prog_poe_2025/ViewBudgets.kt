package com.example.prog_poe_2025
import Data_Classes.Category
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prog_poe_2025.databinding.ActivityViewBudgetsBinding
import androidx.recyclerview.widget.RecyclerView
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
    val totalSpent: Float, // Added this
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

        // Initialize RecyclerView
        budgetsRecyclerView = binding.budgetsRecyclerView
        budgetsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up adapter initially with an empty list
        budgetAdapter = BudgetAdapter(emptyList())
        budgetsRecyclerView.adapter = budgetAdapter

        // Fetch real data from the database
        fetchBudgets()

        // Bottom Navigation setup
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        fetchBudgets()  // Auto-refresh budget list when coming back
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.selectedItemId = R.id.nav_viewBudgets
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> startActivity(Intent(this, LogIncomeExpense::class.java))
                R.id.nav_home -> startActivity(Intent(this, Home::class.java))
                R.id.nav_game -> startActivity(Intent(this, BudgetQuiz::class.java))
            }
            true
        }
    }

    fun fetchBudgets() {
        val userId = SessionManager.getUserId(applicationContext)
        // Use a default start time of 0L ("All") for fetching the baseline data
        val defaultStartTime = 0L

        lifecycleScope.launch(Dispatchers.IO) {
            val budgets = budgetDao.getBudgetsForUser(userId)

            withContext(Dispatchers.Main) {
                if (budgets.isEmpty()) {
                    binding.txtNoBudgetsMessage.apply {
                        visibility = View.VISIBLE
                        text = "No budgets created yet. Add a new budget to start tracking!"
                    }
                    budgetAdapter.updateBudgets(emptyList())
                    budgetsRecyclerView.adapter = budgetAdapter  // Ensure full UI refresh
                    return@withContext
                }

                binding.txtNoBudgetsMessage.visibility = View.GONE
                budgetsRecyclerView.visibility = View.VISIBLE

                val vbBudgetsList = budgets.map { budget ->
                    // Retrieve the budget along with its categories
                    val budgetWithCategories = budgetDao.getBudgetWithCategories(budget.id)

                    // Use the default start time so that all transactions are included globally
                    val spentAmounts = budgetWithCategories.categories.associateWith { category ->
                        val totalSpent = expensesDao.getTotalSpentInCategory(userId, category.name, defaultStartTime) ?: 0f
                        val totalIncome = db.incomeDao().getTotalIncomeInCategory(userId, category.name, defaultStartTime) ?: 0f
                        val adjustedSpent = totalSpent - totalIncome
                        maxOf(adjustedSpent, 0f)
                    }

                    val totalSpent = spentAmounts.values.sum()
                    val remainingAmount = budget.maxMonthGoal - totalSpent

                    VbBudget(budget.id, budget.name, budget.maxMonthGoal, spentAmounts, totalSpent, remainingAmount)
                }

                budgetAdapter.updateBudgets(vbBudgetsList)
                budgetsRecyclerView.adapter = budgetAdapter  // Force full UI refresh if needed
            }
        }
    }
}