package com.example.prog_poe_2025

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.databinding.ReportItemBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportAdapter(private var reportList: List<Report>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA")) // Ensure correct currency format for ZAR

    inner class ReportViewHolder(private val binding: ReportItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.reportBudgetName.text = report.budgetName
            binding.reportMaxAmount.text = "Max budget: ${currencyFormat.format(report.maxAmount)}"

            binding.reportStartDate.text = "Start: ${formatDate(report.startDate)}"
            binding.reportEndDate.text = "End: ${formatDate(report.endDate)}"

            binding.reportHighestExpense.text = "Highest Expense: ${currencyFormat.format(report.highestExpense)}"
            binding.reportHighestIncome.text = "Highest Income: ${currencyFormat.format(report.highestIncome)}"

            binding.reportCategories.text = if (report.categories.isNotEmpty()) {
                "Categories: ${report.categories.joinToString(", ")}"
            } else {
                "Categories: None"
            }
        }

        private fun formatDate(timestamp: Long): String {
            return if (timestamp > 0) dateFormat.format(Date(timestamp)) else "N/A"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reportList[position])
    }

    override fun getItemCount(): Int = reportList.size

    fun updateBudgets(newReport: List<Report>) {
        val diffCallback = ReportDiffCallback(reportList, newReport)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        reportList = newReport
        diffResult.dispatchUpdatesTo(this)
    }
}

class ReportDiffCallback(
    private val oldList: List<Report>,
    private val newList: List<Report>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
/**
 * Source: https://developer.android.com/guide/topics/ui/layout/recyclerview
 * Author: Android Developers (Google)
 * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
 * Adapted by: Ade-Eza & Lusanda or Pennywise
 * Purpose: Implements a RecyclerView Adapter to bind data to list items in a RecyclerView
 * Modifications:
 * - Customized ViewHolder for specific item layout
 */