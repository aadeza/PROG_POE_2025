package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

        setupRecyclerView()
        setupBottomNavigation()
        fetchBudgets()
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
        Log.d("DEBUG", "User ID from SessionManager: $userId")

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val budgetsSnapshot = db.collection("budgets")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()

                if (budgetsSnapshot.isEmpty) {
                    showNoBudgetsMessage("No budgets created yet. Add a new budget to start tracking!")
                    return@launch
                }

                val budgetDocs = budgetsSnapshot.documents

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
                    it.id to Category(it.id, name)
                } ?: emptyMap()

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
                    if (budgetId != null && catId != null && amt != null)
                        Triple(budgetId, catId, amt)
                    else null
                }.groupBy { it.first to it.second }
                    .mapValues { entry -> entry.value.map { it.third }.sum() }


                val vbBudgetsList = budgetDocs.map { bDoc ->
                    val id = bDoc.id
                    val name = bDoc.getString("name") ?: "Unnamed"
                    val maxGoal = bDoc.getLong("maxMonthGoal") ?: 0L
                    val minGoal = bDoc.getLong("minMonthGoal") ?: 0L

                    val categoryIds = (bDoc.get("categories") as? List<*>)
                        ?.mapNotNull { it?.toString() } ?: emptyList()

                    val categories = categoryIds.mapNotNull { cid -> categoryMap[cid] }


                    val spentAmounts = categories.associate { category ->
                        val key = id to category.id

                        category to (expenseSums[key] ?: 0f)

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