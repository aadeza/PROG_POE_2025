package com.example.prog_poe_2025

import Data_Classes.Category
import android.annotation.SuppressLint
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

        holder.checkBox.text = if (isCreateMode) "Create category \"${createCategoryName}\"" else category.name
        holder.checkBox.isChecked = category.selected

        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            category.selected = isChecked

            if (isCreateMode && isChecked) {
                val capitalizedName = createCategoryName.replaceFirstChar { it.uppercase() }
                createListener?.invoke(capitalizedName)
                isCreateMode = false
            }
        }
    }

    override fun getItemCount() = categories.size

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
//GeeksforGeeks(2025)

/*References List
GeeksforGeeks. 2025. SimpleAdapter in Android with Example, n.d. [Online]. Available at:
https://www.geeksforgeeks.org/simpleadapter-in-android-with-example/ [Accessed 25 April 2025].
*/

