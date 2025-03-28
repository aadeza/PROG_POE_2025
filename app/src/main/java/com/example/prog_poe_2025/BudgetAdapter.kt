package com.example.prog_poe_2025

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ItemBudgetBinding





class BudgetAdapter(private val budgetList: List<Budget>) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgetList[position]
        holder.bind(budget)
    }

    override fun getItemCount(): Int = budgetList.size

    class BudgetViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(budget: Budget) {
            binding.budgetName.text = budget.budgetName
            binding.budgetDescription.text = budget.budgetDescription
            binding.totalAmount.text = "Total Budget: R${budget.totalAmount}"
            binding.currentAmount.text = "Remaining: R${budget.currentAmount}"
            binding.startDate.text = "Start Date: ${budget.startDate}"
            binding.endDate.text = "End Date: ${budget.endDate}"
            binding.categories.text = "Categories: ${budget.categories.joinToString(", ")}"
            binding.progress.text = "Spending Progress: ${budget.progress}%"
            binding.lastUpdated.text = "Last Updated: ${budget.lastUpdated}"


            if (budget.currentAmount < 0) {
                binding.overspendingWarning.visibility = View.VISIBLE
            } else {
                binding.overspendingWarning.visibility = View.GONE
            }
        }
    }
}

