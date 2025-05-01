package com.example.prog_poe_2025

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_2025.BudgetAdapter.BudgetViewHolder
import com.example.prog_poe_2025.databinding.ItemBudgetBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import Data_Classes.Category
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.prog_poe_2025.databinding.ReportItemBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.*
import java.util.*
import java.util.Calendar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportAdapter(private var reportList: List<Report>) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val adapterScope = CoroutineScope(Dispatchers.Main)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance() // ✅ Ensures proper formatting for monetary values

    inner class ReportViewHolder(private val binding: ReportItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(report: Report) {
            binding.reportBudgetName.text = report.budgetName
            binding.reportMaxAmount.text = "Max budget amount: ${currencyFormat.format(report.maxAmount)}"
            binding.reportStartDate.text = "Start date: ${dateFormat.format(Date(report.startDate))}"
            binding.reportEndDate.text = "End date: ${dateFormat.format(Date(report.endDate))}"

            // ✅ Display highest expense and highest income with proper currency format
            binding.reportHighestExpense.text = "Highest Expense: ${currencyFormat.format(report.highestExpense)}"
            binding.reportHighestIncome.text = "Highest Income: ${currencyFormat.format(report.highestIncome)}"

            // ✅ Display categories related to the budget safely
            binding.reportCategories.text = if (report.categories.isNotEmpty()) {
                "Categories: ${report.categories.joinToString(", ")}"
            } else {
                "Categories: None"
            }
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
        reportList = newReport
        notifyDataSetChanged() // ✅ Refresh list properly
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        adapterScope.cancel()
    }
}