package com.example.prog_poe_2025

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ItemBudgetBinding
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class BudgetAdapter(
    private var budgetList: List<VbBudget>,
    private val filterHours: Int // ✅ Pass the selected time filter
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: VbBudget) {
            binding.budgetName.text = budget.name
            binding.budgetRange.text = "Budget Range: Min R${budget.minMonthGoal} - Max R${budget.maxMonthGoal}"
            val spent = budget.totalSpent
            val minGoal = budget.minMonthGoal.toFloat()
            val maxGoal = budget.maxMonthGoal.toFloat()

            // Only filter out categories with a zero amount.
            val filteredSpentAmounts = budget.spentAmounts.filter { it.value > 0 }

            setupBarChart(filteredSpentAmounts, budget)
            setupEditButton(budget)
            setupDeleteButton(budget)

            binding.progressWrapper.post {
                updateCustomBar(spent, minGoal, maxGoal)
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
            binding.btnDeleteBudget.setOnClickListener {
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Delete Budget")
                    .setMessage("Are you sure you want to delete this budget?")
                    .setPositiveButton("Delete") { _, _ -> deleteBudget(budget.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun deleteBudget(budgetId: String) {
            db.collection("budgets").document(budgetId).delete()
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "Budget deleted", Toast.LENGTH_SHORT).show()
                    (binding.root.context as? ViewBudgets)?.fetchBudgets(filterHours) // Refresh with selected filter
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }

        private fun setupBarChart(spentMap: Map<Category, Float>, budget: VbBudget) {
            val barChart = binding.barChart as? com.github.mikephil.charting.charts.HorizontalBarChart ?: return

            // Filter only nonzero amounts.
            val filteredSpentMap = spentMap.filter { it.value > 0 }

            // Handle empty state.
            if (filteredSpentMap.isEmpty()) {
                barChart.clear()
                barChart.invalidate()
                binding.barChart.visibility = View.GONE
                binding.noDataMessage.visibility = View.VISIBLE
                return
            }

            binding.barChart.visibility = View.VISIBLE
            binding.noDataMessage.visibility = View.GONE

            // Create bar chart entries from filtered data.
            val entries = filteredSpentMap.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value)
            }

            val dataSet = BarDataSet(entries, "Spent").apply {
                color = Color.BLUE
                valueTextSize = 12f
                setDrawValues(true)
            }

            val barData = BarData(dataSet).apply {
                barWidth = 0.7f
            }

            // Configure bar chart properties.
            barChart.apply {
                clear() // Remove previous data.
                data = barData
                setFitBars(true)
                description.isEnabled = false
                legend.isEnabled = true
                setDrawValueAboveBar(true)
                setDrawBarShadow(false)
                setTouchEnabled(false)

                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(filteredSpentMap.keys.map { it.name })
                    granularity = 1f
                    labelCount = filteredSpentMap.size
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    axisMinimum = -0.5f
                    axisMaximum = filteredSpentMap.size - 0.5f
                }

                axisLeft.apply {
                    removeAllLimitLines()
                    val minLimit = LimitLine(budget.minMonthGoal.toFloat(), "Min").apply {
                        lineColor = Color.GREEN
                        lineWidth = 2f
                    }
                    addLimitLine(minLimit)

                    val maxLimit = LimitLine(budget.maxMonthGoal.toFloat(), "Max").apply {
                        lineColor = Color.RED
                        lineWidth = 2f
                    }
                    addLimitLine(maxLimit)

                    axisMinimum = 0f
                    axisMaximum = maxOf(budget.maxMonthGoal.toFloat(), filteredSpentMap.values.maxOrNull() ?: 0f) * 1.1f

                    setDrawGridLines(true)
                    enableGridDashedLine(10f, 10f, 0f)
                }

                axisRight.isEnabled = false

                animateY(1000) // Smooth update animation.
                notifyDataSetChanged()
                invalidate()
            }
        }

        private fun updateCustomBar(spent: Float, min: Float, max: Float) {
            val wrapper = binding.progressWrapper
            val bar = binding.actualSpendingBar
            val minLine = binding.minLine
            val maxLine = binding.maxLine
            val minLabel = binding.minLabel
            val maxLabel = binding.maxLabel
            val statusText = binding.statusText

            val wrapperWidth = wrapper.width.takeIf { it > 0 } ?: wrapper.measuredWidth
            if (wrapperWidth <= 0) return

            val scaleFactor = 1.2f
            val visualMax = max * scaleFactor

            val barPercentage = (spent / visualMax).coerceIn(0f, 1f)
            val barWidth = (wrapperWidth * barPercentage).toInt()
            val minPosition = (wrapperWidth * (min / visualMax)).toInt()
            val maxPosition = (wrapperWidth * (max / visualMax)).toInt()

            bar.layoutParams.width = barWidth
            bar.requestLayout()

            minLine.translationX = minPosition.toFloat()
            maxLine.translationX = maxPosition.toFloat()

            maxLabel.post {
                maxLabel.translationX = maxPosition.toFloat() - (maxLabel.width / 2)
            }
            minLabel.post {
                minLabel.translationX = minPosition.toFloat() - (minLabel.width / 2)
            }

            minLabel.text = "R${"%.2f".format(min)}"
            maxLabel.text = "R${"%.2f".format(max)}"

            val gradient = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                when {
                    spent < min -> intArrayOf(0xFFFFF176.toInt(), 0xFFFFEB3B.toInt()) // Below goal.
                    spent <= max -> intArrayOf(0xFF81C784.toInt(), 0xFF4CAF50.toInt()) // Within goal.
                    else -> intArrayOf(0xFFE57373.toInt(), 0xFFF44336.toInt()) // Over goal.
                }
            )
            gradient.cornerRadius = bar.height / 2f
            bar.background = gradient

            statusText.text = when {
                spent < min -> "Status: Below Goal"
                spent <= max -> "Status: Within Goal"
                else -> "Status: Over Goal"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgetList[position]) // ✅ Ensure filtering applies dynamically
    }

    override fun getItemCount(): Int = budgetList.size

    fun updateBudgets(newBudgets: List<VbBudget>) {
        budgetList = newBudgets
        notifyDataSetChanged()
    }
}