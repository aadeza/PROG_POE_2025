package com.example.prog_poe_2025

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
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

class BudgetAdapter(private var budgetList: List<VbBudget>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(budget: VbBudget) {
            binding.budgetName.text = budget.name
            binding.budgetRange.text =
                "Budget Range: Min R${budget.minMonthGoal} - Max R${budget.maxMonthGoal}"

            val progress = if (budget.maxMonthGoal > 0)
                ((budget.totalSpent / budget.maxMonthGoal) * 100).toInt().coerceIn(0, 100)
            else 0

            binding.progressBar.progress = progress

            setupBarChart(budget.spentAmounts, budget)
            setupEditButton(budget)
            setupDeleteButton(budget)
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
                    Toast.makeText(binding.root.context, "Budget deleted", Toast.LENGTH_SHORT)
                        .show()
                    (binding.root.context as? ViewBudgets)?.fetchBudgets()
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "Failed to delete", Toast.LENGTH_SHORT)
                        .show()
                }
        }


        private fun setupBarChart(spentMap: Map<Category, Float>, budget: VbBudget) {
            val barChart =
                binding.barChart as? com.github.mikephil.charting.charts.HorizontalBarChart
                    ?: return

            // Create entries: each index maps to a Category
            val entries = spentMap.entries.mapIndexed { index, entry ->
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

            barChart.apply {
                data = barData
                setFitBars(true)
                description.isEnabled = false
                legend.isEnabled = true
                setDrawValueAboveBar(true)
                setDrawBarShadow(false)
                setTouchEnabled(false)



                barChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(spentMap.keys.map { it.name })
                    granularity = 1f
                    labelCount = spentMap.size
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    axisMinimum = -0.5f
                    axisMaximum = spentMap.size - 0.5f
                }


                barChart.axisLeft.apply {
                    removeAllLimitLines()

                    val minLimit = LimitLine(budget.minMonthGoal.toFloat(), "Min")
                    minLimit.lineColor = Color.GREEN
                    minLimit.lineWidth = 2f
                    minLimit.textColor = Color.GREEN
                    minLimit.textSize = 10f
                    addLimitLine(minLimit)

                    val maxLimit = LimitLine(budget.maxMonthGoal.toFloat(), "Max")
                    maxLimit.lineColor = Color.RED
                    maxLimit.lineWidth = 2f
                    maxLimit.textColor = Color.RED
                    maxLimit.textSize = 10f
                    addLimitLine(maxLimit)

                    axisMinimum = 0f
                    axisMaximum = maxOf(budget.maxMonthGoal.toFloat(), spentMap.values.maxOrNull() ?: 0f) * 1.1f

                    setDrawGridLines(true)
                    enableGridDashedLine(10f, 10f, 0f)
                }



                barChart.axisRight.isEnabled = false


                animateY(1000)
                notifyDataSetChanged()
                invalidate()
            }
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