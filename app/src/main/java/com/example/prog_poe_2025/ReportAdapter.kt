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

import java.util.Calendar

class ReportAdapter (private var reportList:List<Report>) :  RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private val adapterScope = CoroutineScope(Dispatchers.Main)

    inner class ReportViewHolder(private val binding: ReportItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(report: Report) {
            binding.reportMaxAmount.text= report.budgetName
            binding.reportMaxAmount.text= "Max budget amount: ${report.maxAmount}"
            binding.reportStartDate.text= "Start date: ${report.startDate}"
            binding.reportEndDate.text="End date: ${report.endDate}"
        }


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding =  ReportItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reportList[position])
    }

    override fun getItemCount(): Int = reportList.size

    fun updateBudgets(newReport: List<Report>) {
        reportList = newReport
        notifyDataSetChanged()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        adapterScope.cancel() // Prevent memory leaks
    }
}