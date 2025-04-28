package com.example.prog_poe_2025

import Data_Classes.Expenses
import Data_Classes.Income
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.Calendar

class LogIncomeExpense : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var toggleIncomeExpense: ToggleButton
    private lateinit var txtAmount: EditText
    private lateinit var txtDescription: EditText
    private lateinit var datePicker: Button
    private lateinit var btnDone: Button
    private lateinit var addImage: ImageView
    private var selectedDateInMillis: Long = System.currentTimeMillis()
    private var selectedImageUri: Uri? = null

    val CatViewModel: CategoryViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_income_expense)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(database.incomeDao(), database.expensesDao())
        val streakRepository = StreakRepository(database.streakDao())
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        lifecycleScope.launch {
            streakRepository.updateStreakAfterLogging()
        }
        toggleIncomeExpense = findViewById(R.id.tgbtnPickIncExp)
        txtAmount = findViewById(R.id.edtName)
        txtDescription = findViewById(R.id.edtTxtMlDescription)
        datePicker = findViewById(R.id.btnLogDate)
        btnDone = findViewById(R.id.btnLogDone)
        addImage = findViewById(R.id.imgLog)
        val categorySpinner: Spinner = findViewById(R.id.spnCategory)

        // Observe categories
        CatViewModel.categories.observe(this) { categories ->
            val categoryNames = categories.map { it.name }.distinct()
            val categoryAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            categorySpinner.adapter = categoryAdapter
        }

        val transactTypeSpinner: Spinner = findViewById(R.id.spnTransactType)

        val transactionTypes = listOf(
            "Cash",
            "Debit Card",
            "Credit Card",
            "Mobile Payment",
            "Gift Card",
            "Bank Transfer"
        )
        val transactTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            transactionTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        transactTypeSpinner.adapter = transactTypeAdapter

        datePicker.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build().apply {
                    show(supportFragmentManager, "startDatePicker")

                    addOnPositiveButtonClickListener { selectedDateInMillis ->
                        this@LogIncomeExpense.selectedDateInMillis = selectedDateInMillis
                        showTimePicker(selectedDateInMillis) // now it's valid
                    }
                }
        }

        addImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnDone.setOnClickListener {
            val amountText = txtAmount.text.toString()
            val description = txtDescription.text.toString()
            val category = categorySpinner.selectedItem.toString()
            val transactionType = transactTypeSpinner.selectedItem.toString()
            val userId = 1

            if (amountText.isBlank()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toLongOrNull()
            if (amount == null) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (toggleIncomeExpense.isChecked) {
                    val income = Income(
                        amount = amount,
                        description = description,
                        category = category,
                        date = selectedDateInMillis,
                        transaction_type = transactionType,
                        imagePath = selectedImageUri?.toString(),
                        user_id = userId
                    )
                    viewModel.saveIncome(income)
                    Toast.makeText(this@LogIncomeExpense, "Income saved", Toast.LENGTH_SHORT).show()
                } else {
                    val expense = Expenses(
                        amount = amount,
                        description = description,
                        category = category,
                        date = selectedDateInMillis,
                        transaction_type = transactionType,
                        imagePath = selectedImageUri?.toString(),
                        user_id = userId
                    )
                    viewModel.saveExpense(expense)
                    Toast.makeText(this@LogIncomeExpense, "Expense saved", Toast.LENGTH_SHORT).show()
                }

                // ✅ After successfully saving income/expense, update streak
                streakRepository.updateStreakAfterLogging()

                // Clear fields
                txtAmount.text.clear()
                txtDescription.text.clear()
                selectedImageUri = null
            }
        }


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_transaction
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_viewBudgets -> {
                    startActivity(Intent(this, ViewBudgets::class.java))
                    true
                }
                R.id.nav_game -> {
                    startActivity(Intent(this, BudgetQuiz::class.java))
                    true
                }
                else -> false
            }
        }
    }


    private fun showTimePicker(selectedDateInMillis: Long) {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Select Time of Transaction")
            .build()

        timePicker.show(supportFragmentManager, "timePicker")

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateInMillis
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)

            val fullDateTimeInMillis = calendar.timeInMillis

            Toast.makeText(this, "Date and Time selected", Toast.LENGTH_SHORT).show()

            this.selectedDateInMillis = fullDateTimeInMillis
        }
    }
}