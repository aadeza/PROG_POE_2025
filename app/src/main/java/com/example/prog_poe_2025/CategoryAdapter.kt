package com.example.prog_poe_2025

import Data_Classes.Category
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var categories: List<Category> // List of Category objects
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
        val categoryName = category.name

        holder.checkBox.text = categoryName

        // Optionally set check status if category is selected
        holder.checkBox.isChecked = category.selected // Assuming you have a 'selected' property

        holder.checkBox.setOnCheckedChangeListener(null) // Remove previous listener to avoid duplication
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            category.selected = isChecked // Update the category selected state
        }
    }

    override fun getItemCount() = categories.size

    // Method to update the data
    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged() // Notify the RecyclerView that data has changed
    }
}
