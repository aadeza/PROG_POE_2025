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
            //Log.d("DEBUG", "Total Spent: ${budget.totalSpent} | Remaining: ${budget.remainingAmount}")

            binding.budgetName.text = budget.name
            binding.maxBudgetGoal.text = "Max Budget Goal: R${budget.maxMonthGoal}"

            //binding.txtSpent.text = "Spent: R${budget.totalSpent}"
            //binding.remainingAmount.text = "Remaining: R${budget.remainingAmount}"

            val progress = (budget.totalSpent / budget.maxMonthGoal * 100).coerceIn(0F, 100F)
            binding.progressBar.progress = progress.toInt()

            setupPieChart(budget.spentAmounts)
            setupDateFilterSpinner(budget)
            setupEditButton(budget)
            setupDeleteButton(budget)
        }
        private fun setupPieChart(spentAmounts: Map<Category, Float>) {
            Log.d("DEBUG", "Setting up Pie Chart with: $spentAmounts")

            val entries = spentAmounts.filter { it.value > 0 }
                .map { (category, amount) -> PieEntry(amount, category.name) }

            Log.d("DEBUG", "Pie Chart Entries Before Setup: $entries")

            if (entries.isEmpty()) {
                Log.d("DEBUG", "Pie Chart has NO valid data. Keeping previous data.")
                return // ✅ Don't reset the pie chart if no new data exists!
            }

            Log.d("DEBUG", "Pie Chart Entries After Filtering: $entries")

            binding.pieChart.visibility = View.VISIBLE
            binding.pieChart.clear() // ✅ Clears old data only when necessary

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)
                sliceSpace = 2f
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${String.format("%.1f", value)}%" // ✅ Rounds to 1 decimal place and adds "%"
                    }
                }
            }

            binding.pieChart.apply {
                data = PieData(dataSet)
                description.isEnabled = false
                setUsePercentValues(true)
                animateY(1000)
                Log.d("DEBUG", "Pie Chart Updated and Visible")
                invalidate()
            }
        }

        private fun setupDateFilterSpinner(budget: VbBudget) {
            val dateOptions = listOf("Last Hour", "Last 12 Hours", "Today", "Week", "Month", "Year", "All")
            val dateAdapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_item, dateOptions
            )
            dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinDateFilter.adapter = dateAdapter

            binding.spinDateFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    val selectedFilter = parent.getItemAtPosition(position)?.toString() ?: "All"
                    filterExpenses(selectedFilter, budget)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        private fun setupEditButton(budget: VbBudget) {
            binding.btnEditBudget.setOnClickListener {
                val intent = Intent(it.context, EditBudget::class.java)
                intent.putExtra("budgetId", budget.id)
                it.context.startActivity(intent)
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
                .setPositiveButton("Delete") { _, _ ->
                    deleteBudget(budgetId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteBudget(budgetId: Int) {
            val db = AppDatabase.getDatabase(binding.root.context)
            val budgetDao = db.budgetDao()

            CoroutineScope(Dispatchers.IO).launch { // Run deletion in the background
                budgetDao.deleteBudgetById(budgetId)

                withContext(Dispatchers.Main) {
                    Toast.makeText(binding.root.context, "Budget deleted successfully!", Toast.LENGTH_SHORT).show()
                    (binding.root.context as? ViewBudgets)?.fetchBudgets()
                }
            }
        }

        private fun filterExpenses(selectedFilter: String, budget: VbBudget) {
            val startTime = getStartTimeMillis(selectedFilter)
            val userId = SessionManager.getUserId(binding.root.context)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(binding.root.context)
                val budgetWithCategories = db.budgetDao().getBudgetWithCategories(budget.id)
                val budgetId = SessionManager.getSelectedBudgetId(binding.root.context)

                val spentAmounts = budgetWithCategories.categories.associateWith { category ->
                    val totalSpent = db.expensesDao().getTotalSpentInCategory(userId, category.name, budgetId, startTime) ?: 0f
                    val totalIncome = db.incomeDao().getTotalIncomeInCategory(userId, category.name, budgetId, startTime) ?: 0f
                    val finalSpent = maxOf(totalSpent - totalIncome, 0f)

                    Log.d("DEBUG", "Filtering Category: ${category.name} - Total Spent: $totalSpent, Total Income: $totalIncome, Final Spent: $finalSpent") // ✅ Logs filter results

                    finalSpent
                }

                val totalSpent = spentAmounts.values.sum()
                val remainingAmount = budget.maxMonthGoal - totalSpent

                Log.d("DEBUG", "Updating UI - Filtered Total Spent: $totalSpent, Remaining: $remainingAmount") // ✅ Confirms correct transaction retrieval

                withContext(Dispatchers.Main) {
                    //binding.txtSpent.text = "Spent: R${String.format("%.2f", totalSpent)}"
                    //binding.remainingAmount.text = "Remaining: R${String.format("%.2f", remainingAmount)}"

                    setupPieChart(spentAmounts) // ✅ Ensure the pie chart updates dynamically
                }
            }
        }

        private fun getStartTimeMillis(filter: String): Long {
            val calendar = Calendar.getInstance()
            when (filter) {
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
        val binding =
            ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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