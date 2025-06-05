package com.example.prog_poe_2025


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class EditBudget : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var budgetTypeCat: Spinner
    private lateinit var budgetName: EditText
    private lateinit var btnSelectDate: Button
    private lateinit var txtDateRange: TextView
    private lateinit var btnUpdateBudget: Button
    private lateinit var edtMinGoal: EditText
    private lateinit var edtMaxGoal: EditText

    // Use a String for Firestore document ID.
    private var budgetId: String = ""
    private var startDateMillis: Long = 0
    private var endDateMillis: Long = 0
    private var allCategories = listOf<Category>()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_budget)

        // Retrieve budget ID from the intent (expecting a String).
        budgetId = intent.getStringExtra("budgetId") ?: ""
        if (budgetId.isEmpty()) {
            Toast.makeText(this, "Invalid budget ID.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI elements.
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        budgetTypeCat = findViewById(R.id.spinCategories)
        budgetName = findViewById(R.id.edtNameBudget)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        txtDateRange = findViewById(R.id.txtDateRange)
        edtMinGoal = findViewById(R.id.edtMinGoal)
        edtMaxGoal = findViewById(R.id.edtMaxGoal)
        btnUpdateBudget = findViewById(R.id.btnUpdateBudget)

        // Load budget data from Firestore.
        loadBudgetDetails()

        btnSelectDate.setOnClickListener { showStartDatePicker() }
        btnUpdateBudget.setOnClickListener { updateBudget() }

        // Handle category search/creation.
        searchEditText.addTextChangedListener {
            val query = it.toString().trim()
            val filtered = allCategories.filter { cat -> cat.name.contains(query, ignoreCase = true) }
            if (filtered.isEmpty() && query.isNotEmpty()) {
                val capitalized = query.split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { ch -> ch.uppercase() }
                }
                val tempCategory = Category(name = capitalized, selected = false)
                categoryAdapter.setCreateMode(true, capitalized)
                categoryAdapter.updateData(mutableListOf(tempCategory))
            } else {
                categoryAdapter.setCreateMode(false, "")
                categoryAdapter.updateData(filtered.toMutableList())
            }
        }
    }

    private fun loadBudgetDetails() {
        lifecycleScope.launch {
            try {
                // Fetch the budget document from Firestore.
                val docSnapshot = db.collection("budgets").document(budgetId).get().await()
                if (docSnapshot != null && docSnapshot.exists()) {
                    // Populate budget fields.
                    budgetName.setText(docSnapshot.getString("name") ?: "")
                    edtMinGoal.setText(docSnapshot.getLong("minMonthGoal")?.toString() ?: "0")
                    edtMaxGoal.setText(docSnapshot.getLong("maxMonthGoal")?.toString() ?: "0")
                    startDateMillis = docSnapshot.getLong("startDate") ?: 0L
                    endDateMillis = docSnapshot.getLong("endDate") ?: 0L

                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    txtDateRange.text = "From ${sdf.format(Date(startDateMillis))} to ${sdf.format(Date(endDateMillis))}"

                    // Setup the budget type spinner.
                    val budgetTypes = listOf("Personal Budget", "Business Budget", "Event & Special Purpose Budget", "Savings Budget")
                    val spinnerAdapter = ArrayAdapter(this@EditBudget, android.R.layout.simple_spinner_item, budgetTypes)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    budgetTypeCat.adapter = spinnerAdapter
                    val budgetType = docSnapshot.getString("budgetType") ?: "Personal Budget"
                    budgetTypeCat.setSelection(budgetTypes.indexOf(budgetType))

                    // Load budget categories.
                    // Assume the "categories" field is stored as a List<String> of category IDs.
                    val budgetCategoriesIds = docSnapshot.get("categories") as? List<String> ?: emptyList()

                    // Load all categories from Firestore.
                    val categoriesSnapshot = db.collection("categories").get().await()
                    allCategories = categoriesSnapshot.documents.map { document ->
                        Category(
                            id = document.id,
                            name = document.getString("name") ?: "Unknown",
                            selected = false
                        )
                    }
                    // Mark the categories that are assigned to this budget.
                    allCategories = allCategories.map { cat ->
                        cat.copy(selected = budgetCategoriesIds.contains(cat.id))
                    }

                    // Setup the CategoryAdapter.
                    categoryAdapter = CategoryAdapter(allCategories.toMutableList())
                    categoryRecyclerView.adapter = categoryAdapter
                    categoryRecyclerView.layoutManager = LinearLayoutManager(this@EditBudget)
                    categoryAdapter.updateData(allCategories.toMutableList())
                } else {
                    Toast.makeText(this@EditBudget, "Budget not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditBudget, "Error loading budget details: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Source: https://firebase.google.com/docs/firestore/manage-data/add-data
     * Author: Firebase Documentation (Google)
     * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
     * Adapted by: Ade-Eza Silongo for Pennywise
     * Purpose: Saves data to Firebase Firestore database
     * Modifications:
     * - Wrapped data saving in Kotlin coroutine for asynchronous operation
     * - Added custom error handling
     */
    private fun updateBudget() {
        val budgetNameText = budgetName.text.toString()
        val budgetTypeText = budgetTypeCat.selectedItem.toString()
        val minGoal = edtMinGoal.text.toString().toLongOrNull() ?: 0L
        val maxGoal = edtMaxGoal.text.toString().toLongOrNull() ?: 0L
        val selectedCategories = categoryAdapter.getSelectedCategories()

        if (budgetNameText.isBlank() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one category.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Build a map for the updated budget.
                val updatedBudget = hashMapOf(
                    "name" to budgetNameText,
                    "budgetType" to budgetTypeText,
                    "startDate" to startDateMillis,
                    "endDate" to endDateMillis,
                    "minMonthGoal" to minGoal,
                    "maxMonthGoal" to maxGoal,
                    "categories" to selectedCategories.map { it.id },
                    "userId" to SessionManager.getUserId(applicationContext)
                )

                // Update the budget document.
                db.collection("budgets").document(budgetId).update(updatedBudget as Map<String, Any>).await()

                Toast.makeText(this@EditBudget, "Budget updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditBudget, "Error updating budget: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStartDatePicker() {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Start Date")
            .build().apply {
                show(supportFragmentManager, "startDatePicker")
                addOnPositiveButtonClickListener {
                    startDateMillis = it
                    showEndDatePicker(it)
                }
            }
    }

    private fun showEndDatePicker(minDate: Long) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select End Date")
            .setSelection(minDate + 86400000) // Ensures end date is after start date.
            .setCalendarConstraints(CalendarConstraints.Builder().setStart(minDate).build())
            .build().apply {
                show(supportFragmentManager, "endDatePicker")
                addOnPositiveButtonClickListener {
                    endDateMillis = it
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    txtDateRange.text = "From ${sdf.format(Date(startDateMillis))} to ${sdf.format(Date(endDateMillis))}"
                }
            }
    }
}