package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ActivityGenreportBinding
import com.example.prog_poe_2025.databinding.ActivityViewBudgetsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.time.LocalDate

data class Report(
    val id: Int,
    val budgetName: String,
    val maxAmount: Long,
    val startDate:Long,
    val endDate: Long,
    val highestExpense: Float,
    val highestIncome: Float,
    val categories: List<String>,
    val isExpense: Boolean
)

class GenReport : AppCompatActivity() {

    private lateinit var binding: ActivityGenreportBinding
    private lateinit var reportRecyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val budgetDAO by lazy { db.budgetDao() }
    private val userDAO by lazy { db.userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenreportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        setupRecyclerView()

        // Fetch user details
        fetchUserInfo()

        // Generate report (conditionally based on budget availability)
        generateReport()

        // ✅ Initialize Bottom Navigation
        val bottomNavigationView = findViewById<    BottomNavigationView>(R.id.bottom_navigation)

// ✅ Highlight Home tab when viewing reports
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Already on this screen
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
            val dialogView = LayoutInflater.from(this).inflate(R.layout.transaction_popup, null)
            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)

            val dialog = dialogBuilder.create()
            dialog.show()

            val recyclerTransactions = dialogView.findViewById<RecyclerView>(R.id.recyclerTransactions)
            val btnClosePopup = dialogView.findViewById<Button>(R.id.btnClosePopup)

            recyclerTransactions.layoutManager = LinearLayoutManager(this)
            lifecycleScope.launch(Dispatchers.IO) {
                val expenses = db.expensesDao().getExpensesByUser(SessionManager.getUserId(applicationContext)) // ✅ No need for copy()
                val income = db.incomeDao().getIncomeByUser(SessionManager.getUserId(applicationContext)) // ✅ No need for copy()

                val allTransactions = (expenses + income).sortedByDescending { it.date.toLong() } // ✅ Sort transactions

                withContext(Dispatchers.Main) {
                    recyclerTransactions.adapter = TransactionAdapter(allTransactions, false) // ✅ Uses the data model's built-in `isExpense`
                }
            }

            btnClosePopup.setOnClickListener { dialog.dismiss() }
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

        lifecycleScope.launch(Dispatchers.IO) {
            val user = userDAO.getUserById(userID)

            withContext(Dispatchers.Main) {
                user?.let {
                    binding.txtName.text = "Name: ${it.name}"
                    binding.txtSurname.text = "Surname: ${it.surname}"
                    binding.txtUsername.text = "Username: ${it.email}"
                    binding.txtStreakProgress.text = "Current streak progress: N/A"
                    binding.txtQuizScore.text = "Average quiz score: N/A"
                } ?: run {
                    Toast.makeText(applicationContext, "User info not found.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun generateReport() {
        val userID = SessionManager.getUserId(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val budgets = budgetDAO.getBudgetsForUser(userID) // ✅ Correct query

            withContext(Dispatchers.Main) {
                if (budgets.isEmpty()) {
                    binding.txtBudgetsCreated.text = "Budgets created: 0"
                    Toast.makeText(applicationContext, "No information available to generate a report.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.txtBudgetsCreated.text = "Budgets created: ${budgets.size}"

                    // ✅ Convert Budgets to Report format
                    val reports = budgets.map { budget ->
                        val highestExpense = (budgetDAO.getHighestExpense(budget.id) ?: 0).toFloat()
                        val highestIncome = (budgetDAO.getHighestIncome(budget.id) ?: 0).toFloat()
                        val categories = budgetDAO.getCategoriesForBudget(budget.id).map { it.name }

                        Report(
                            id = budget.id,
                            budgetName = budget.name,
                            maxAmount = budget.maxMonthGoal,
                            startDate = budget.startDate,
                            endDate = budget.endDate,
                            highestExpense = highestExpense,
                            highestIncome = highestIncome,
                            categories = categories,
                            isExpense = highestExpense > highestIncome
                        )
                    }

                    reportAdapter.updateBudgets(reports) // ✅ Fixed type mismatch
                }
            }
        }
    }

}