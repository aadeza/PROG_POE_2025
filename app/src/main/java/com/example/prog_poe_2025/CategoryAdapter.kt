package com.example.prog_poe_2025

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: MutableList<String>,  // Change to MutableList
    private val selectedCategories: MutableList<String>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.categoryCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.checkBox.text = category
        holder.checkBox.isChecked = selectedCategories.contains(category)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedCategories.add(category)
            } else {
                selectedCategories.remove(category)
            }
        }
    }

    override fun getItemCount() = categories.size

    // This method allows you to update the categories list
    fun updateData(newCategories: List<String>) {
        categories.clear()  // Clear the current list
        categories.addAll(newCategories)  // Add new data to the list
        notifyDataSetChanged()  // Notify the adapter that the data has changed
    }
}
