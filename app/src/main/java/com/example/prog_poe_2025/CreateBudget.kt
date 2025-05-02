package com.example.prog_poe_2025

import Data_Classes.BudgetCategoryCrossRef
import Data_Classes.Budgets
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
import android.annotation.SuppressLint
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

    class CreateBudget : AppCompatActivity() {

        // 🔧 UI Elements
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

        //  ViewModel
        private lateinit var categoryViewModel: CategoryViewModel

        //  Categories
        private var allCategories = listOf<Category>()

        // Dates
        private var startDateMillis: Long = 0
        private var endDateMillis: Long = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_create_budget)

            // Initialize Views
            categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
            searchEditText = findViewById(R.id.searchEditText)
            budgetTypeCat = findViewById(R.id.spinCategories)
            budgetName = findViewById(R.id.edtNameBudget)
            btnSelectDate = findViewById(R.id.btnSelectDate)
            txtDateRange = findViewById(R.id.txtDateRange)
            edtMinGoal = findViewById(R.id.edtMinGoal)
            edtMaxGoal = findViewById(R.id.edtMaxGoal)
            btnAddBudget = findViewById(R.id.btnAddBudget)

            //  Setup Budget Type Spinner
            val budgetTypes = listOf(
                "Personal Budget",
                "Business Budget",
                "Event & Special Purpose Budget",
                "Savings Budget"
            )
            budgetTypeCat.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                budgetTypes
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            //  ViewModel
            categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]//Medium(2023)




            //  Observe Live Category Data
            categoryViewModel.categories.observe(this) { categories ->
                allCategories = categories
                categoryAdapter = CategoryAdapter(categories.toMutableList())
                categoryRecyclerView.adapter = categoryAdapter
                categoryRecyclerView.layoutManager = LinearLayoutManager(this)

                // 🛠️ Handle category creation from search
                categoryAdapter.setOnCreateCategoryListener { newCategoryName ->
                    val existingCategory = allCategories.find { it.name.equals(newCategoryName, ignoreCase = true) }

                    if (existingCategory == null) { // Only insert if category does NOT exist
                        val newCategory = Category(name = newCategoryName, selected = true)
                        categoryViewModel.insert(newCategory)

                        lifecycleScope.launch {
                            delay(300)
                            val updatedList = categoryViewModel.categories.value.orEmpty()
                            categoryAdapter.updateData(updatedList.toMutableList())
                        }

                        Toast.makeText(this, "Category \"$newCategoryName\" created!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Category \"$newCategoryName\" already exists!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Handle Search Filtering + Category Creation
            searchEditText.addTextChangedListener {
                val query = it.toString().trim()
                val filtered = allCategories.filter { cat -> cat.name.contains(query, ignoreCase = true) }

                if (filtered.isEmpty() && query.isNotEmpty()) {
                    val capitalized = query.split(" ").joinToString(" ") { word -> word.replaceFirstChar(Char::uppercase) }
                    val tempCategory = Category(name = capitalized, selected = false)
                    categoryAdapter.setCreateMode(true, capitalized)
                    categoryAdapter.updateData(mutableListOf(tempCategory))
                } else {
                    categoryAdapter.setCreateMode(false, "")
                    categoryAdapter.updateData(filtered.toMutableList())
                }
            }

            // Select Date Range
            btnSelectDate.setOnClickListener { showStartDatePicker() }

            // Create Budget
            btnAddBudget.setOnClickListener { showConfirmDialog() }
        }


        //Start Date Picker
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
        }/* TutorialsPoint(2025)
        */

        // End Date Picker
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

        // Confirmation Dialog
        private fun showConfirmDialog() {
            val budgetNameText = budgetName.text.toString()
            val budgetTypeText = budgetTypeCat.selectedItem.toString()
            val dateRangeText = txtDateRange.text.toString()
            val minGoalText = edtMinGoal.text.toString()
            val maxGoalText = edtMaxGoal.text.toString()
            val selectedCategories = categoryAdapter.getSelectedCategories()

            if (budgetNameText.isBlank() || minGoalText.isBlank() || maxGoalText.isBlank() || selectedCategories.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields and select at least one category.", Toast.LENGTH_SHORT).show()
                return
            }

            AlertDialog.Builder(this)
                .setTitle("Confirm Budget Details")
                .setMessage("""
                    Budget Name: $budgetNameText
                    Budget Type: $budgetTypeText
                    Date Range: $dateRangeText
                    Min Goal: $minGoalText
                    Max Goal: $maxGoalText
                    Categories: ${selectedCategories.joinToString { it.name }}
                """.trimIndent())
                .setPositiveButton("Confirm and Create") { dialog, _ ->
                    createBudget()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // 🚀 Create and Save Budget
        private fun createBudget() {
            val budgetNameText = budgetName.text.toString()
            val budgetTypeText = budgetTypeCat.selectedItem.toString()
            val minGoal = edtMinGoal.text.toString().toLongOrNull() ?: 0
            val maxGoal = edtMaxGoal.text.toString().toLongOrNull() ?: 0
            val selectedCategories = categoryAdapter.getSelectedCategories()
            val userId = SessionManager.getUserId(applicationContext)

            if (userId != -1) {
                lifecycleScope.launch {
                    val budget = Budgets(
                        budgetType = budgetTypeText,
                        name = budgetNameText,
                        startDate = startDateMillis,
                        endDate = endDateMillis,
                        minMonthGoal = minGoal,
                        maxMonthGoal = maxGoal,
                        user_id = userId
                    )
                    val budgetId = categoryViewModel.insertBudget(budget).toInt()
                    val crossRefs = selectedCategories.map {
                        BudgetCategoryCrossRef(budgetId = budgetId, categoryId = it.id)
                    }
                    categoryViewModel.insertBudgetCategoryCrossRefs(crossRefs)
                    Toast.makeText(this@CreateBudget, "Budget created!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }//(W3Schools,2025)

/*References List
Svaghasiya, 2023. Using ViewModel in Android With Kotlin, 18 September 2023. [Online]. Available at:
https://medium.com/@ssvaghasiya61/using-viewmodel-in-android-with-kotlin-16ca735c644f [Accessed 25 April 2025].

TutorialsPoint, 2025. Android- Date Picker, n.d. [Online]. Available at:
https://www.tutorialspoint.com/android/android_datepicker_control.htm [Accessed 21 April 2025].
[3]W3Schools, 2025. Kotlin Tutorial, n.d. [Online]. Available at:
https://www.w3schools.com/kotlin/index.php [Accessed 25 April 2025].
*/



