package com.example.prog_poe_2025

import DAOs.ExpensesDAO
import DAOs.IncomeDAO
import Data_Classes.Expenses
import Data_Classes.Income
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LogIncomeExpense : AppCompatActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private lateinit var viewModel: TransactionViewModel

    private lateinit var edtName: EditText
    private lateinit var edtTxtMlDescription: EditText
    private lateinit var spnCategory: Spinner
    private lateinit var spnTransactType: Spinner
    private lateinit var btnLogDate: Button
    private lateinit var btnLogDone: Button
    private lateinit var toggleButton: ToggleButton

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var imagePath: String? = null

    private val IMAGE_PICK_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_income_expense)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize fields
        edtName = findViewById(R.id.edtName)
        edtTxtMlDescription = findViewById(R.id.edtTxtMlDescription)
        spnCategory = findViewById(R.id.spnCategory)
        spnTransactType = findViewById(R.id.spnTransactType)
        btnLogDate = findViewById(R.id.btnLogDate)
        btnLogDone = findViewById(R.id.btnLogDone)
        toggleButton = findViewById(R.id.tgbtnPickIncExp)
        val imgLogButton = findViewById<ImageButton>(R.id.imgLog)

        // Initialize Database and ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TransactionRepository(database.incomeDao(), database.expensesDao())
        val streakRepository = StreakRepository(database.streakDao())
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        lifecycleScope.launch {
            streakRepository.updateStreakAfterLogging()
        }

        imgLogButton.setOnClickListener { openImageGallery() }
        btnLogDate.setOnClickListener { showDatePicker() }

        // Transaction Type Spinner
        val transactionTypes = resources.getStringArray(R.array.transaction_types)
        val transactionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionTypes)
        transactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnTransactType.adapter = transactionAdapter

        // 🔥 Watch Categories
        categoryViewModel.categories.observe(this) { categories ->
            if (categories.isEmpty()) {
                showNoCategoriesDialog()
            } else {
                val categoryNames = categories.map { it.name }
                categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnCategory.adapter = categoryAdapter
            }
        }

        // Save Button Click
        btnLogDone.setOnClickListener {
            saveTransaction(database.incomeDao(), database.expensesDao())
        }

        // Bottom Nav
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

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            findViewById<TextView>(R.id.txtSelectedDate).text = "Selected Date: $selectedDate"

            selectedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            findViewById<TextView>(R.id.txtSelectedTime).text = "Selected Time: $selectedTime"
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun openImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_REQUEST) {
            val imageUri: Uri? = data?.data
            findViewById<ImageView>(R.id.imgSelectedImage).setImageURI(imageUri)
            imagePath = imageUri?.toString()
        }
    }

    private fun saveTransaction(incomeDao: IncomeDAO, expensesDao: ExpensesDAO) {
        val amountText = edtName.text.toString()
        val description = edtTxtMlDescription.text.toString()
        val category = spnCategory.selectedItem?.toString() ?: ""
        val transactionType = spnTransactType.selectedItem.toString()

        if (amountText.isEmpty() || selectedDate == null || selectedTime == null || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toLongOrNull() ?: 0L
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fullDateTime = formatter.parse("$selectedDate $selectedTime")
        val timestamp = fullDateTime?.time ?: System.currentTimeMillis()
        val userId = 1

        lifecycleScope.launch {
            if (toggleButton.isChecked) {
                val income = Income(
                    amount = amount,
                    description = description,
                    category = category,
                    date = timestamp,
                    transaction_type = transactionType,
                    imagePath = imagePath,
                    user_id = userId
                )
                incomeDao.insertIncome(income)
                Toast.makeText(this@LogIncomeExpense, "Income saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                val expense = Expenses(
                    amount = amount,
                    description = description,
                    category = category,
                    date = timestamp,
                    transaction_type = transactionType,
                    imagePath = imagePath,
                    user_id = userId
                )
                expensesDao.insertExpense(expense)
                Toast.makeText(this@LogIncomeExpense, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
            }
            clearFields()
        }
    }

    private fun clearFields() {
        edtName.text.clear()
        edtTxtMlDescription.text.clear()
        selectedDate = null
        selectedTime = null
        findViewById<TextView>(R.id.txtSelectedDate).text = "Selected Date:"
        findViewById<TextView>(R.id.txtSelectedTime).text = "Selected Time:"
        findViewById<ImageView>(R.id.imgSelectedImage).setImageDrawable(null)
    }

    private fun showNoCategoriesDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Categories Found")
            .setMessage("You don't have any categories yet. Would you like to create a budget now?")
            .setPositiveButton("Yes") { dialog, _ ->
                startActivity(Intent(this, CreateBudget::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                Toast.makeText(this, "You must create a budget before logging a transaction!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
