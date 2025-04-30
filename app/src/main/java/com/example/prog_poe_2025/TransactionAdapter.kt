package com.example.prog_poe_2025

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class TransactionAdapter(private val transactionList: List<TransactionItem>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryTextView: TextView = view.findViewById(R.id.tvCategory)
        val amountTextView: TextView = view.findViewById(R.id.tvAmount)
        val dateTextView: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        when (transaction) {
            is TransactionItem.IncomeItem -> {
                holder.categoryTextView.text = "Income: ${transaction.income.category}"
                holder.amountTextView.text = String.format("R%.2f", transaction.income.amount.toDouble())
                holder.amountTextView.setTextColor(holder.itemView.context.getColor(R.color.darker_gray))
                holder.dateTextView.text = formatDate(transaction.income.date)
            }
            is TransactionItem.ExpenseItem -> {
                holder.categoryTextView.text = "Expense: ${transaction.expense.category}"
                holder.amountTextView.text = String.format("-R%.2f", transaction.expense.amount.toDouble())
                holder.amountTextView.setTextColor(holder.itemView.context.getColor(R.color.darker_gray))
                holder.dateTextView.text = formatDate(transaction.expense.date)
            }
        }
    }

    override fun getItemCount(): Int = transactionList.size

    private fun formatDate(date: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(date))
    }
}