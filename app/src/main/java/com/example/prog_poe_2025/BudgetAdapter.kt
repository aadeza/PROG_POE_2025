package com.example.prog_poe_2025

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ItemBudgetBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter


class BudgetAdapter(private val budgetList: List<VbBudget>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(budget: VbBudget) {
            binding.budgetName.text = budget.name

            val totalSpent = budget.spentAmounts.values.sum()
            binding.totalAmount.text = "Total: R${budget.totalBudget}"
            binding.txtSpent.text = "Spent: R$totalSpent"

            val progress = if (budget.totalBudget > 0) {
                (totalSpent / budget.totalBudget * 100).toInt()
            } else 0
            binding.progressBar.progress = progress

            // Setup pie chart
            val entries = ArrayList<PieEntry>()
            for ((category, amount) in budget.spentAmounts) {
                entries.add(PieEntry(amount, category))
            }

            val dataSet = PieDataSet(entries, "")
            dataSet.colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)
            dataSet.sliceSpace = 2f
            dataSet.valueTextSize = 12f
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "R$value"
                }
            }

            val pieData = PieData(dataSet)
            binding.pieChart.data = pieData
            binding.pieChart.description.isEnabled = false
            binding.pieChart.invalidate() // Refresh
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
}

