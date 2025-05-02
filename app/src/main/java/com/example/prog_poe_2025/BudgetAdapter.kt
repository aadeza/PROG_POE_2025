package com.example.prog_poe_2025

import Data_Classes.Category
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ItemBudgetBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.*
import java.util.Calendar

class BudgetAdapter(private var budgetList: List<VbBudget>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: VbBudget) {
            Log.d("DEBUG", "Binding Budget ID: ${budget.id}, Name: ${budget.name}")
            Log.d("DEBUG", "Spent Amounts Map: ${budget.spentAmounts}")

            binding.budgetName.text = budget.name
            binding.maxBudgetGoal.text = "Max Budget Goal: R${budget.maxMonthGoal}"

            // Calculate progress percentage based on total spent relative to max goal
            val progress = (budget.totalSpent / budget.maxMonthGoal * 100).coerceIn(0F, 100F)
            binding.progressBar.progress = progress.toInt()

            // Setup UI components
            setupPieChart(budget.spentAmounts)
            setupDateFilterSpinner(budget)
            setupEditButton(budget)
            setupDeleteButton(budget)
        }

        private fun setupPieChart(spentAmounts: Map<Category, Float>) {
            Log.d("DEBUG", "Setting up Pie Chart with: $spentAmounts")

            // Convert non-zero spent amounts into Pie chart entries
            val entries = spentAmounts.filter { it.value > 0 }
                .map { (category, amount) -> PieEntry(amount, category.name) }

            Log.d("DEBUG", "Pie Chart Entries Before Setup: $entries")
            if (entries.isEmpty()) {
                Log.d("DEBUG", "Pie Chart has NO valid data. Keeping previous chart data.")
                return // Do not update the chart if there are no valid entries.
            }

            binding.pieChart.apply {
                visibility = View.VISIBLE
                clear()
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)
                    sliceSpace = 2f
                    valueTextSize = 12f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "${String.format("%.1f", value)}%"
                        }
                    }
                }
                data = PieData(dataSet)
                description.isEnabled = false
                setUsePercentValues(true)
                animateY(1000)
                invalidate()
                Log.d("DEBUG", "Pie Chart Updated and Visible")
            }
        }

        private fun setupDateFilterSpinner(budget: VbBudget) {
            val dateOptions = listOf(
                "Last 3 Minutes",
                "Last Hour",
                "Last 12 Hours",
                "Today",
                "Week",
                "Month",
                "Year",
                "All"
            )
            val context = binding.root.context
            val dateAdapter =
                ArrayAdapter(context, android.R.layout.simple_spinner_item, dateOptions)
            dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinDateFilter.adapter = dateAdapter

            binding.spinDateFilter.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedFilter = parent.getItemAtPosition(position)?.toString() ?: "All"
                        filterExpenses(selectedFilter, budget)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }

        private fun setupEditButton(budget: VbBudget) {
            binding.btnEditBudget.setOnClickListener { view ->
                val intent = Intent(view.context, EditBudget::class.java).apply {
                    putExtra("budgetId", budget.id)
                }
                view.context.startActivity(intent)
            }
        }

        private fun setupDeleteButton(budget: VbBudget) {
            binding.btnDeleteBudget.setOnClickListener {
                showDeleteConfirmationDialog(budget.id)
            }
        }

        private fun showDeleteConfirmationDialog(budgetId: Int) {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete") { _, _ -> deleteBudget(budgetId) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteBudget(budgetId: Int) {
            val context = binding.root.context
            val db = AppDatabase.getDatabase(context)
            val budgetDao = db.budgetDao()

            CoroutineScope(Dispatchers.IO).launch {
                budgetDao.deleteBudgetById(budgetId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Budget deleted successfully!", Toast.LENGTH_SHORT)
                        .show()
                    (context as? ViewBudgets)?.fetchBudgets()
                }
            }
        }

        private fun filterExpenses(selectedFilter: String, budget: VbBudget) {
            val context = binding.root.context
            val startTime = getStartTimeMillis(selectedFilter)
            val userId = SessionManager.getUserId(context)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val budgetWithCategories = db.budgetDao().getBudgetWithCategories(budget.id)
                val budgetId = SessionManager.getSelectedBudgetId(context)

                // Compute net spent for each category (expenses minus income)
                val spentAmounts = budgetWithCategories.categories.associateWith { category ->
                    val totalSpent =
                        db.expensesDao()
                            .getTotalSpentInCategory(userId, category.name, budgetId, startTime)
                            ?: 0f
                    val totalIncome =
                        db.incomeDao()
                            .getTotalIncomeInCategory(userId, category.name, budgetId, startTime)
                            ?: 0f
                    maxOf(totalSpent - totalIncome, 0f)
                }

                val totalSpentSum = spentAmounts.values.sum()
                val remainingAmount = budget.maxMonthGoal - totalSpentSum

                Log.d(
                    "DEBUG",
                    "Updating UI - Filtered Total Spent: $totalSpentSum, Remaining: $remainingAmount"
                )

                withContext(Dispatchers.Main) {
                    // Update the pie chart based on the filtered data
                    setupPieChart(spentAmounts)
                }
            }
        }

        private fun getStartTimeMillis(filter: String): Long {
            val calendar = Calendar.getInstance()
            when (filter) {
                "Last 3 Minutes" -> calendar.add(
                    Calendar.MINUTE,
                    -3
                ) // ✅ Adds support for 3-minute filtering
                "Last Hour" -> calendar.add(Calendar.HOUR, -1)
                "Last 12 Hours" -> calendar.add(Calendar.HOUR, -12)
                "Today" -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                }

                "Week" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
                "Month" -> calendar.add(Calendar.MONTH, -1)
                "Year" -> calendar.add(Calendar.YEAR, -1)
                "All" -> return 0L
            }
            return calendar.timeInMillis
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgetList[position])
    }

    override fun getItemCount(): Int = budgetList.size

    fun updateBudgets(newBudgets: List<VbBudget>) {
        budgetList = newBudgets
        notifyDataSetChanged()
    }
}

// (W3Schools,2025)

/*
Reference List:
W3Schools. 2025. Kotlin Tutorial, n.d.[Online]. Available at:
https://www.w3schools.com/kotlin/index.php  [Accessed 20 April 2025].
 */