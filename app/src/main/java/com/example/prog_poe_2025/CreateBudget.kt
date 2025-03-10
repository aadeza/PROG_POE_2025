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

class CreateBudget : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var spinnerCategories: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget) // Ensure the correct layout file is used

        // Initialize views
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        spinnerCategories = findViewById(R.id.spinCategories) // Spinner reference

        // List of categories for the spinner
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

        // Set up RecyclerView
        val categories = mutableListOf(
            "Transport",
            "Food",
            "Entertainment",
            "Healthcare",
            "School",
            "Utilities",
            "Rent",
            "Groceries",
            "Clothing",
            "Personal Care",
            "Fitness",
            "Subscriptions",
            "Insurance",
            "Savings",
            "Travel",
            "Gifts",
            "Events",
            "Dining Out",
            "Business Expenses",
            "Pets",
            "Home Improvement",
            "Mortgage",
            "Childcare",
            "Education",
            "Technology",
            "Phone & Internet",
            "Entertainment Streaming",
            "Debt Repayment",
            "Loans",
            "Taxes",
            "Charity"
        )
        val selectedCategories = mutableListOf<String>()

        // Setup RecyclerView with LinearLayoutManager and Adapter
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(categories, selectedCategories)
        categoryRecyclerView.adapter = categoryAdapter

        // Filter categories based on search input
        searchEditText.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isEmpty()) {
                // If the search field is empty, show all categories
                categoryAdapter.updateData(categories)
            } else {
                // Filter the categories based on the search query
                val filteredCategories = categories.filter {
                    it.contains(query, ignoreCase = true)
                }
                categoryAdapter.updateData(filteredCategories)
            }
        }


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

