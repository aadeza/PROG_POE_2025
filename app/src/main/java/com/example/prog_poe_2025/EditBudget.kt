package com.example.prog_poe_2025

import Data_Classes.BudgetCategoryCrossRef
import Data_Classes.Budgets
import Data_Classes.Category
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
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

    private var budgetId: Int = -1
    private var startDateMillis: Long = 0
    private var endDateMillis: Long = 0
    private var allCategories = listOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_budget)

        // Retrieve budget ID from intent
        budgetId = intent.getIntExtra("budgetId", -1)
        if (budgetId == -1) {
            Toast.makeText(this, "Invalid budget ID.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI elements
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        budgetTypeCat = findViewById(R.id.spinCategories)
        budgetName = findViewById(R.id.edtNameBudget)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        txtDateRange = findViewById(R.id.txtDateRange)
        edtMinGoal = findViewById(R.id.edtMinGoal)
        edtMaxGoal = findViewById(R.id.edtMaxGoal)
        btnUpdateBudget = findViewById(R.id.btnUpdateBudget)

        // Load budget data
        loadBudgetDetails()

        btnSelectDate.setOnClickListener { showStartDatePicker() }
        btnUpdateBudget.setOnClickListener { updateBudget() }

        // Handle category creation in search
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
    }

    private fun loadBudgetDetails() {
        lifecycleScope.launch {
            val budget = AppDatabase.getDatabase(this@EditBudget).budgetDao().getBudgetById(budgetId)
            budget?.let {
                budgetName.setText(it.name)
                edtMinGoal.setText(it.minMonthGoal.toString())
                edtMaxGoal.setText(it.maxMonthGoal.toString())
                startDateMillis = it.startDate
                endDateMillis = it.endDate

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                txtDateRange.text = "From ${sdf.format(startDateMillis)} to ${sdf.format(endDateMillis)}"

                val budgetTypes = listOf("Personal Budget", "Business Budget", "Event & Special Purpose Budget", "Savings Budget")
                val adapter = ArrayAdapter(this@EditBudget, android.R.layout.simple_spinner_item, budgetTypes)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                budgetTypeCat.adapter = adapter
                budgetTypeCat.setSelection(budgetTypes.indexOf(it.budgetType))

                val categories = AppDatabase.getDatabase(this@EditBudget).budgetDao().getCategoriesForBudget(it.id)
                allCategories = AppDatabase.getDatabase(this@EditBudget).categoryDao().getAllCategories()
                categoryAdapter = CategoryAdapter(allCategories.toMutableList())
                categoryRecyclerView.adapter = categoryAdapter
                categoryRecyclerView.layoutManager = LinearLayoutManager(this@EditBudget)
                categoryAdapter.setSelectedCategories(categories)
            }
        }
    }

    private fun updateBudget() {
        val budgetNameText = budgetName.text.toString()
        val budgetTypeText = budgetTypeCat.selectedItem.toString()
        val minGoal = edtMinGoal.text.toString().toLongOrNull() ?: 0
        val maxGoal = edtMaxGoal.text.toString().toLongOrNull() ?: 0
        val selectedCategories = categoryAdapter.getSelectedCategories()

        if (budgetNameText.isBlank() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields and select at least one category.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val updatedBudget = Budgets(
                id = budgetId,
                budgetType = budgetTypeText,
                name = budgetNameText,
                startDate = startDateMillis,
                endDate = endDateMillis,
                minMonthGoal = minGoal,
                maxMonthGoal = maxGoal,
                user_id = SessionManager.getUserId(applicationContext)
            )

            val db = AppDatabase.getDatabase(this@EditBudget)
            db.budgetDao().updateBudget(updatedBudget)

            db.budgetDao().deleteBudgetCategoryCrossRefsForBudget(budgetId)
            val crossRefs = selectedCategories.map { BudgetCategoryCrossRef(budgetId = budgetId, categoryId = it.id) }
            db.budgetDao().insertBudgetCategoryCrossRefs(crossRefs)

            Toast.makeText(this@EditBudget, "Budget updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
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
            .setSelection(minDate + 86400000) // Ensures end date is after start date
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
}//(W3Schools,2205)

/*Reference List
W3Schools, 2025. Kotlin Tutorial, n.d. [Online]. Available at:
https://www.w3schools.com/kotlin/index.php [Accessed 19 April 2025].
*/
