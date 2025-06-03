package com.example.prog_poe_2025

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val categoryMap: Map<String, String>
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

        // 🔹 Ensure category details are displayed
        val category = transaction.categoryId?.let { categoryMap[it] } ?: "Unknown"
        val amount = transaction.amount
        val dateString = formatDate(transaction.date)

        // ✅ Set text FIRST to ensure visibility
        holder.txtDetails.text = "$category – R${"%.2f".format(amount)} ($dateString)"
        holder.txtDetails.setTextColor(if (transaction.isExpense) Color.RED else Color.GREEN)
        holder.txtDetails.visibility = View.VISIBLE

        // 🔹 Load image using Glide
        val imageUrl = transaction.imageUrl ?: ""
        if (imageUrl.isNotEmpty()) {
            holder.imgTransaction.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholderimage)
                .error(R.drawable.placeholderimage)
                .into(holder.imgTransaction)
        } else {
            holder.imgTransaction.setImageResource(R.drawable.placeholderimage)
            holder.imgTransaction.visibility = View.VISIBLE
        }
    }
    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        val diffCallback = TransactionDiffCallback(transactions, newTransactions)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        transactions = newTransactions
        diffResult.dispatchUpdatesTo(this)
    }

    private fun formatDate(timestamp: Long): String {
        return if (timestamp > 0) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            formatter.format(Date(timestamp))
        } else {
            "Unknown date"
        }
    }

    class TransactionDiffCallback(
        private val oldList: List<Transaction>,
        private val newList: List<Transaction>
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
}

//(W3Schools,2025)

/*References List
GeeksforGeeks. 2025. SimpleAdapter in Android with Example, n.d. [Online]. Available at:
https://www.geeksforgeeks.org/simpleadapter-in-android-with-example/ [Accessed 25 April 2025].

W3Schools. 2025. Kotlin Tutorial, n.d.[Online]. Available at:
https://www.w3schools.com/kotlin/index.php  [Accessed 24 April 2025].
*/
