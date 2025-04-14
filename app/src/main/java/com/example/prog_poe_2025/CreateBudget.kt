package com.example.prog_poe_2025

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.launch
import Data_Classes.Category

class CreateBudget : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var spinnerCategories: Spinner
    private lateinit var categoryViewModel: CategoryViewModel
    private var allCategories = listOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget)

        // Initialize views
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        spinnerCategories = findViewById(R.id.spinCategories)

        // Setup the Spinner with budget types
        val budgetTypes = listOf(
            "Personal Budget",
            "Business Budget",
            "Event & Special Purpose Budget",
            "Savings Budget"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            budgetTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCategories.adapter = spinnerAdapter

        // Initialize CategoryViewModel
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)

        // Prefill categories if the table is empty
        lifecycleScope.launch {
            val categoriesFromDb = categoryViewModel.categories.value.orEmpty()
            if (categoriesFromDb.isEmpty()) {
                // Insert default categories
                val defaultCategories = listOf(
                    Category(name = "Food"),
                    Category(name = "Transport"),
                    Category(name = "Entertainment"),
                    Category(name = "Utilities")
                )
                categoryViewModel.insertAll(defaultCategories)  // Insert default categories
            }
        }

        // Observe the categories LiveData from ViewModel
        categoryViewModel.categories.observe(this, { categories ->
            // Update the RecyclerView when categories change
            allCategories = categories
            categoryAdapter = CategoryAdapter(categories.toMutableList())
            categoryRecyclerView.adapter = categoryAdapter
        })

        // Setup RecyclerView
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)

        // Filter categories based on search input
        searchEditText.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isEmpty()) {
                // If the search field is empty, show all categories from the database
                categoryAdapter.updateData(allCategories)
            } else {
                // Filter the categories based on the search query
                val filteredCategories = allCategories.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                categoryAdapter.updateData(filteredCategories)
            }
        }

        // Spinner Item selection listener
        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedBudgetType = budgetTypes[position]
                // Handle the selected budget type
                Toast.makeText(this@CreateBudget, "Selected: $selectedBudgetType", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle the case when nothing is selected
            }
        }
    }
}




