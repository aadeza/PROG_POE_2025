package com.example.prog_poe_2025

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<spTransaction>,
    private val isExpenseList: Boolean // ✅ Determines if transactions are expenses or income
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDetails: TextView = itemView.findViewById(R.id.txtTransactionDetails)
        val imgTransaction: ImageView = itemView.findViewById(R.id.imgTransaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.txtDetails.text = "${transaction.category} - R${transaction.amount} (${formatDate(transaction.date)})"

        // 🔥 Apply color based on whether this is an Expense or Income
        holder.txtDetails.setTextColor(if (transaction.isExpense) Color.RED else Color.GREEN) // ✅ Uses transaction type instead of toggle state

        // ✅ Load image safely and handle exceptions
        if (!transaction.imagePath.isNullOrEmpty()) {
            try {
                holder.imgTransaction.visibility = View.VISIBLE
                holder.imgTransaction.setImageURI(Uri.parse(transaction.imagePath))
            } catch (e: SecurityException) {
                Log.e("IMAGE_ERROR", "Error loading image: ${e.message}")
                holder.imgTransaction.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("IMAGE_ERROR", "Unexpected error loading image: ${e.message}")
                holder.imgTransaction.visibility = View.GONE
            }
        } else {
            holder.imgTransaction.visibility = View.GONE
        }
    }

    override fun getItemCount() = transactions.size

    // ✅ Format date for better readability
    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}