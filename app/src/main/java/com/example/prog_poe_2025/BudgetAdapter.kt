package com.example.prog_poe_2025

import android.content.Intent
import android.graphics.Color
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class BudgetAdapter(private var budgetList: List<VbBudget>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var filteredSpentAmounts: Map<Category, Float>? = null

        fun bind(budget: VbBudget) {
            binding.budgetName.text = budget.name
            binding.maxBudgetGoal.text = "Max Budget Goal: R${budget.maxMonthGoal}"

            val totalSpent = filteredSpentAmounts?.values?.sum() ?: budget.totalSpent
            binding.progressBar.progress = ((totalSpent / budget.maxMonthGoal) * 100).toInt()

            setupPieChart(filteredSpentAmounts ?: budget.spentAmounts)
            setupDateFilterSpinner(budget)
            setupEditButton(budget)
            setupDeleteButton(budget)
        }

        private fun setupPieChart(spentAmounts: Map<Category, Float>) {
            val entries = spentAmounts.filter { it.value > 0 }
                .map { (category, amount) -> PieEntry(amount, category.name) }

            if (entries.isEmpty()) return

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)
                sliceSpace = 2f
                valueTextSize = 12f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${"%.1f".format(value)}%"
                }
            }

            binding.pieChart.apply {
                visibility = View.VISIBLE
                data = PieData(dataSet)
                description.isEnabled = false
                setUsePercentValues(true)
                animateY(1000)
                invalidate()
            }
        }

        private fun setupDateFilterSpinner(budget: VbBudget) {
            val dateOptions = listOf("Last Minute", "Last 3 Minutes", "Last Hour", "Today", "Week", "Month", "Year", "All")
            val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, dateOptions).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            binding.spinDateFilter.adapter = adapter
            binding.spinDateFilter.setSelection(dateOptions.indexOf("All"))
            binding.spinDateFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val filter = parent.getItemAtPosition(position) as String
                    filterExpenses(filter, budget)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        private fun setupEditButton(budget: VbBudget) {
            binding.btnEditBudget.setOnClickListener {
                val intent = Intent(binding.root.context, EditBudget::class.java).apply {
                    putExtra("budgetId", budget.id)
                }
                binding.root.context.startActivity(intent)
            }
        }

        private fun setupDeleteButton(budget: VbBudget) {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete") { _, _ -> deleteBudget(budget.id.toString()) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteBudget(budgetId: String) {
            db.collection("budgets").document(budgetId).delete()
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "Budget deleted", Toast.LENGTH_SHORT).show()
                    (binding.root.context as? ViewBudgets)?.fetchBudgets()
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }

        private fun filterExpenses(selectedFilter: String, budget: VbBudget) {
            val context = binding.root.context
            val userId = SessionManager.getUserId(context)
            val startTime = getStartTimeMillis(selectedFilter)

            CoroutineScope(Dispatchers.IO).launch {
                val categories = budget.categories // Should be List<Category>
                val spentMap = mutableMapOf<Category, Float>()

                val deferreds = categories.map { category ->
                    async {
                        val snapshot = db.collection("expenses")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("budgetId", budget.id)
                            .whereEqualTo("category", category.name)
                            .whereGreaterThan("timestamp", startTime)
                            .get()
                            .await()

                        val totalSpent = snapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }
                        spentMap[category] = totalSpent.toFloat()
                    }
                }

                deferreds.awaitAll()

                val totalFilteredSpent = spentMap.values.sum()
                val progress = ((totalFilteredSpent / budget.maxMonthGoal) * 100).coerceIn(0f, 100f)

                withContext(Dispatchers.Main) {
                    setupPieChart(spentMap)
                    binding.progressBar.progress = progress.toInt()
                    if (totalFilteredSpent == 0f) {
                        Toast.makeText(context, "No transactions for '$selectedFilter'", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun getStartTimeMillis(filter: String): Long {
            val calendar = Calendar.getInstance()
            when (filter) {
                "Last Minute" -> calendar.add(Calendar.MINUTE, -1)
                "Last 3 Minutes" -> calendar.add(Calendar.MINUTE, -3)
                "Last Hour" -> calendar.add(Calendar.HOUR, -1)
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

