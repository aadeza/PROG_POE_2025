package com.example.prog_poe_2025

import android.widget.AdapterView
import android.widget.Toast
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import Data_Classes.Category


class CreateBudget : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var spinnerCategories: Spinner
    private var allCategories = listOf<Category>()  // Hold all categories as Category objects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget) // Ensure the correct layout file is used

        // Initialize views
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        spinnerCategories = findViewById(R.id.spinCategories) // Spinner reference

        val budgetTypes = listOf(
            "Personal Budget",
            "Business Budget",
            "Event & Special Purpose Budget",
            "Savings Budget"
        )

        // Setup the Spinner with ArrayAdapter
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            budgetTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerCategories.adapter = spinnerAdapter

        // Initialize database and Category DAO
        val db = AppDatabase.getDatabase(this)
        val categoryDao = db.categoryDao()

        // Populate RecyclerView with categories from the database
        lifecycleScope.launch {
            // Fetch categories from the database
            val categoriesFromDb = categoryDao.getAllCategories()  // List of Category objects
            allCategories = categoriesFromDb  // Store all categories for future filtering

            // Update the adapter with Category objects
            categoryAdapter = CategoryAdapter(categoriesFromDb.toMutableList()) // Pass Category objects
            categoryRecyclerView.adapter = categoryAdapter
        }

        // Setup RecyclerView with LinearLayoutManager and Adapter
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
                parentView: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedBudgetType = budgetTypes[position]
                // Handle the selected budget type
                Toast.makeText(
                    this@CreateBudget,
                    "Selected: $selectedBudgetType",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle the case when nothing is selected
            }
        }
    }
}




