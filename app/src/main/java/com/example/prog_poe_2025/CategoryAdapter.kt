package com.example.prog_poe_2025

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private var categories: MutableList<Category>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var isCreateMode = false
    private var createCategoryName: String = ""
    private var createListener: ((String) -> Unit)? = null

    fun setCreateMode(state: Boolean, name: String) {
        isCreateMode = state
        createCategoryName = name
    }

    fun setOnCreateCategoryListener(listener: (String) -> Unit) {
        createListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.categoryCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        val isCreating = isCreateMode && category.name == createCategoryName

        holder.checkBox.setOnCheckedChangeListener(null) // prevent recycled listener bug

        holder.checkBox.text = if (isCreating) {
            "Create category \"$createCategoryName\""
        } else {
            category.name
        }

        holder.checkBox.isChecked = category.selected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isCreating && isChecked) {
                // Trigger category creation
                val capitalizedName = createCategoryName.trim().split(" ")
                    .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

                createListener?.invoke(capitalizedName)

                // Reset mode
                isCreateMode = false
                createCategoryName = ""
            } else {
                category.selected = isChecked
            }
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    fun getSelectedCategories(): List<Category> {
        return categories.filter { it.selected }
    }

    fun setSelectedCategories(selectedCategories: List<Category>) {
        categories.forEach { category ->
            category.selected = selectedCategories.any { it.id == category.id }
        }
        notifyDataSetChanged()
    }
}

/**
 * Source: https://developer.android.com/guide/topics/ui/layout/recyclerview
 * Author: Android Developers (Google)
 * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
 * Adapted by: Ade-Eza Silongo or Pennywise
 * Purpose: Implements a RecyclerView Adapter to bind data to list items in a RecyclerView
 * Modifications:
 * - Customized ViewHolder for specific item layout
 */