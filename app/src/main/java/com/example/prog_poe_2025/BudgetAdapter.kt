package com.example.prog_poe_2025

import Data_Classes.Category
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val adapterScope = CoroutineScope(Dispatchers.Main) // Prevent memory leaks

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: VbBudget) {
            binding.budgetName.text = budget.name
            binding.maxBudgetGoal.text = "Max Budget Goal: R${budget.maxMonthGoal}"
            // Use baseline data passed from ViewBudgets.kt (fetched with a default filter)
            val adjustedSpent = budget.spentAmounts.values.sum()
            binding.txtSpent.text = "Spent: R${adjustedSpent}"
            binding.remainingAmount.text = "Remaining: R${budget.remainingAmount}"

            val progress = if (budget.maxMonthGoal > 0) {
                (adjustedSpent / budget.maxMonthGoal * 100).toInt()
            } else 0
            binding.progressBar.progress = progress

            setupPieChart(budget.spentAmounts)
            setupDateFilterSpinner(budget)
            setupEditButton(budget)
            setupDeleteButton(budget)
        }

        private fun setupPieChart(spentAmounts: Map<Category, Float>) {
            val entries = spentAmounts
                .filter { it.value > 0 }
                .map { (category, amount) -> PieEntry(amount, category.name) }

            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)
                    sliceSpace = 2f
                    valueTextSize = 12f
                    // Ensure the correct method is overridden for your MPAndroidChart version.
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "R$value"
                        }
                    }
                }
                val pieData = PieData(dataSet)
                binding.pieChart.apply {
                    data = pieData
                    description.isEnabled = false
                    setUsePercentValues(true)
                    animateY(1000)
                    invalidate()
                }
            } else {
                binding.pieChart.clear()
                binding.pieChart.setNoDataText("No spending data available.")
            }
        }

        private fun setupDateFilterSpinner(budget: VbBudget) {
            val dateOptions =
                listOf("Last Hour", "Last 12 Hours", "Today", "Week", "Month", "Year", "All")
            val dateAdapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_item, dateOptions
            )
            dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinDateFilter.adapter = dateAdapter

            binding.spinDateFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            // Delete the budget; per-item deletion may also handle related categories if needed.
            val db = AppDatabase.getDatabase(binding.root.context)
            val budgetDao = db.budgetDao()

            adapterScope.launch(Dispatchers.IO) {
                budgetDao.deleteBudgetById(budgetId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        binding.root.context,
                        "Budget deleted successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Refresh the list by calling fetchBudgets() in ViewBudgets.
                    (binding.root.context as? ViewBudgets)?.fetchBudgets()
                }
            }
        }

        private fun filterExpenses(selectedFilter: String, budget: VbBudget) {
            // Use the spinner's selected filter to determine the effective start time.
            val startTime = getStartTimeMillis(selectedFilter)
            val userId = SessionManager.getUserId(binding.root.context)

            adapterScope.launch {
                val db = AppDatabase.getDatabase(binding.root.context)
                val budgetWithCategories = db.budgetDao().getBudgetWithCategories(budget.id)
                val budgetId = SessionManager.getSelectedBudgetId(binding.root.context) 

                val spentAmounts = budgetWithCategories.categories.associateWith { category ->
                    db.expensesDao().getTotalSpentInCategory(userId, category.name, budgetId,0L)
                        ?: 0f
                }
                val totalSpent = spentAmounts.values.sum()
                val remainingAmount = budget.maxMonthGoal - totalSpent

                withContext(Dispatchers.Main) {
                    binding.txtSpent.text = "Spent: R$totalSpent"
                    binding.remainingAmount.text = "Remaining: R$remainingAmount"
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

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        adapterScope.cancel() // Prevent memory leaks
    }
}