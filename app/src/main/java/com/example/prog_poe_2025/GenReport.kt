package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ActivityGenreportBinding
import com.example.prog_poe_2025.databinding.ActivityViewBudgetsBinding
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
    val endDate: Long
)

class GenReport : AppCompatActivity() {

private lateinit var binding: ActivityGenreportBinding
private lateinit var reportRecyclerView: RecyclerView
private lateinit var reportAdapter: ReportAdapter

//Database initialization
private val db by lazy{AppDatabase.getDatabase(this)}
    private val budgetDAO by lazy { db.budgetDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genreport)

        binding = ActivityGenreportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView
        reportRecyclerView = binding.budgetRecyclerView
        reportRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up adapter initially with an empty list
        reportAdapter = ReportAdapter(emptyList())
        reportRecyclerView.adapter = reportAdapter
        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
generateReport()
        // Initialize RecyclerView
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        generateReport() // ✅ Auto-refresh budget list
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

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.budgetRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

    }

    private fun generateReport() {
        val userID = SessionManager.getUserId(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val report = budgetDAO.getBudgetById(userID)

            report?.let {
                val genReport = Report(
                    id = it.id,
                    budgetName = it.name,
                    maxAmount = it.maxMonthGoal,
                    startDate = it.startDate,
                    endDate = it.endDate
                )

                withContext(Dispatchers.Main) {
                    // Wrap in a list to pass to adapter
                    reportAdapter.updateBudgets(listOf(genReport))
                }
            } ?: withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "No budget found", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

