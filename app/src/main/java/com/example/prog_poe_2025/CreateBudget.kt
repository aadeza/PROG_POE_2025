package com.example.prog_poe_2025

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class CreateBudget : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var searchEditText: EditText
    private lateinit var budgetTypeCat: Spinner
    private lateinit var budgetName: EditText
    private lateinit var btnSelectDate: Button
    private lateinit var txtDateRange: TextView
    private lateinit var btnAddBudget: Button
    private lateinit var edtMinGoal: EditText
    private lateinit var edtMaxGoal: EditText

    private var allCategories = mutableListOf<Category>()
    private var startDateMillis: Long = 0
    private var endDateMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget)

        db = FirebaseFirestore.getInstance()

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        budgetTypeCat = findViewById(R.id.spinCategories)
        budgetName = findViewById(R.id.edtNameBudget)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        txtDateRange = findViewById(R.id.txtDateRange)
        edtMinGoal = findViewById(R.id.edtMinGoal)
        edtMaxGoal = findViewById(R.id.edtMaxGoal)
        btnAddBudget = findViewById(R.id.btnAddBudget)

        val budgetTypes = listOf("Personal Budget", "Business Budget", "Event & Special Purpose Budget", "Savings Budget")
        budgetTypeCat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, budgetTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        setupCategoryRecycler()
        fetchCategories()

        searchEditText.addTextChangedListener { filterCategories(it.toString().trim()) }
        btnSelectDate.setOnClickListener { showStartDatePicker() }
        btnAddBudget.setOnClickListener { showConfirmDialog() }
    }

    private fun setupCategoryRecycler() {
        categoryAdapter = CategoryAdapter(mutableListOf())
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryRecyclerView.adapter = categoryAdapter

        categoryAdapter.setOnCreateCategoryListener { newName ->
            val exists = allCategories.any { it.name.equals(newName, ignoreCase = true) }
            if (!exists) {
                val newCategory = hashMapOf("name" to newName)
                db.collection("categories").add(newCategory).addOnSuccessListener {
                    Toast.makeText(this, "Category \"$newName\" created!", Toast.LENGTH_SHORT).show()
                    fetchCategories() // Refresh list after creation
                }
            } else {
                Toast.makeText(this, "Category \"$newName\" already exists!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun fetchCategories() {
        db.collection("categories").get().addOnSuccessListener { result ->
            allCategories.clear()
            for (doc in result) {
                val category = Category(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    selected = false
                )
                allCategories.add(category)
            }
            categoryAdapter.updateData(allCategories.toMutableList())
        }
    }

    private fun filterCategories(query: String) {
        val filtered = allCategories.filter { it.name.contains(query, ignoreCase = true) }
        if (filtered.isEmpty() && query.isNotEmpty()) {
            val capitalized = query.split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
            val tempCategory = Category(name = capitalized, selected = false)
            categoryAdapter.setCreateMode(true, capitalized)
            categoryAdapter.updateData(mutableListOf(tempCategory))
        } else {
            categoryAdapter.setCreateMode(false, "")
            categoryAdapter.updateData(filtered.toMutableList())
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

    @SuppressLint("SetTextI18n")
    private fun showEndDatePicker(minDate: Long) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select End Date")
            .setSelection(minDate + 86400000)
            .setCalendarConstraints(CalendarConstraints.Builder().setStart(minDate).build())
            .build().apply {
                show(supportFragmentManager, "endDatePicker")
                addOnPositiveButtonClickListener {
                    endDateMillis = it
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    txtDateRange.text = "From ${sdf.format(startDateMillis)} to ${sdf.format(endDateMillis)}"
                }
            }
    }

    private fun showConfirmDialog() {
        val name = budgetName.text.toString()
        val type = budgetTypeCat.selectedItem.toString()
        val dateRange = txtDateRange.text.toString()
        val minGoal = edtMinGoal.text.toString()
        val maxGoal = edtMaxGoal.text.toString()
        val selectedCats = categoryAdapter.getSelectedCategories()

        if (name.isBlank() || minGoal.isBlank() || maxGoal.isBlank() || selectedCats.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one category.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Budget")
            .setMessage("""
                Name: $name
                Type: $type
                Dates: $dateRange
                Min Goal: $minGoal
                Max Goal: $maxGoal
                Categories: ${selectedCats.joinToString { it.name }}
            """.trimIndent())
            .setPositiveButton("Confirm") { dialog, _ ->
                createBudget(name, type, minGoal.toLong(), maxGoal.toLong(), selectedCats)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
    private fun createBudget(name: String, type: String, minGoal: Long, maxGoal: Long, categories: List<Category>) {
        val userId = SessionManager.getUserId(applicationContext)

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val budget = hashMapOf(
            "name" to name,
            "budgetType" to type,
            "startDate" to startDateMillis,
            "endDate" to endDateMillis,
            "minMonthGoal" to minGoal,
            "maxMonthGoal" to maxGoal,
            "user_id" to userId,
            "categories" to categories.map { it.id } // Store category IDs
        )

        db.collection("budgets")
            .add(budget)
            .addOnSuccessListener {
                Toast.makeText(this, "Budget created!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create budget", Toast.LENGTH_SHORT).show()
            }
    }
}
