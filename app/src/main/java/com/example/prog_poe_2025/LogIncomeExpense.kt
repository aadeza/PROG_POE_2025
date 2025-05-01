package com.example.prog_poe_2025

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
import DAOs.ExpensesDAO
import DAOs.IncomeDAO
import Data_Classes.Category
import Data_Classes.Expenses
import Data_Classes.Income
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogIncomeExpense : AppCompatActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private lateinit var edtName: EditText
    private lateinit var edtTxtMlDescription: EditText
    private lateinit var spnCategory: Spinner
    private lateinit var spnTransactType: Spinner
    private lateinit var btnLogDate: Button
    private lateinit var btnLogDone: Button
    private lateinit var toggleButton: ToggleButton
    private lateinit var recyclerTransactions: RecyclerView

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var imagePath: String? = null
    private var selectedBudgetId: Int = 0

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

        // 🔹 Initialize DAOs
        val db = AppDatabase.getDatabase(this)
        val expensesDao = db.expensesDao()
        val incomeDao = db.incomeDao()

        // 🔹 Initialize UI elements
        edtName = findViewById(R.id.edtName)
        edtTxtMlDescription = findViewById(R.id.edtTxtMlDescription)
        spnCategory = findViewById(R.id.spnCategory)
        spnTransactType = findViewById(R.id.spnTransactType)
        btnLogDate = findViewById(R.id.btnLogDate)
        btnLogDone = findViewById(R.id.btnLogDone)
        toggleButton = findViewById(R.id.tgbtnPickIncExp)
        recyclerTransactions = findViewById(R.id.recyclerTransactions)

        val imgLogButton = findViewById<ImageButton>(R.id.imgLog)
        imgLogButton.setOnClickListener { openImageGallery() }

        // 🔹 Determine whether transactions are expenses or income based on toggle button state
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch(Dispatchers.IO) { // ✅ Runs database queries in the background
                val transactions = if (isChecked) expensesDao.getAllExpenses() else incomeDao.getAllIncome()

                withContext(Dispatchers.Main) { // ✅ Switches back to the main thread for UI updates
                    recyclerTransactions.adapter = TransactionAdapter(transactions, isChecked)
                }
            }
        }

        btnLogDate.setOnClickListener { showDatePicker() }

        // 🔹 Set up transaction type spinner
        val transactionTypes = resources.getStringArray(R.array.transaction_types)
        val transactTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionTypes)
        transactTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnTransactType.adapter = transactTypeAdapter

        // 🔹 Initialize ViewModel for categories
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        // 🔥 WATCH THE CATEGORIES LIST
        categoryViewModel.categories.observe(this, Observer { categories ->
            if (categories.isEmpty()) {
                showNoCategoriesDialog()
            } else {
                val categoryNames = categories.map { it.name }
                val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnCategory.adapter = categoryAdapter
            }
        })

        // 🔹 Handle Save button
        btnLogDone.setOnClickListener {
            saveTransaction(incomeDao, expensesDao)
        }

        // 🔹 Bottom Navigation
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

        selectedBudgetId = SessionManager.getSelectedBudgetId(applicationContext)
    }


    private fun saveTransaction(incomeDao: IncomeDAO, expensesDao: ExpensesDAO) {
        val amountText = edtName.text.toString()
        val description = edtTxtMlDescription.text.toString()
        val category = spnCategory.selectedItem?.toString() ?: ""
        val transactionType = spnTransactType.selectedItem?.toString() ?: ""


        if (amountText.isEmpty() || selectedDate.isNullOrEmpty() || selectedTime.isNullOrEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toLongOrNull() ?: 0L
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        // ✅ Parse selected date & time safely
        val fullDateTime = formatter.parse("$selectedDate $selectedTime")
        val timestamp = fullDateTime?.time ?: System.currentTimeMillis()

        val userId = SessionManager.getUserId(applicationContext)
        val safeImagePath = imagePath ?: ""

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@LogIncomeExpense)

            // ✅ Ensure user exists before proceeding
            val userExists = db.userDao().getUserById(userId)
            if (userExists == null) {
                Log.e("DB_ERROR", "User ID $userId not found! Cannot insert transaction.")
                Toast.makeText(this@LogIncomeExpense, "Invalid user! Please log in again.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 🔥 Ensure at least one valid budget exists for the category
            val budgetsForCategory = db.budgetDao().getBudgetsForCategory(category)
            if (budgetsForCategory.isEmpty()) {
                Toast.makeText(this@LogIncomeExpense, "No budget found for this category!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            for (budget in budgetsForCategory) {
                val budgetId = budget.id

                // ✅ Validate that `budgetId` exists in database
                val budgetExists = db.budgetDao().getBudgetById(budgetId)
                if (budgetExists == null) {
                    Log.e("DB_ERROR", "Budget ID $budgetId not found! Cannot insert transaction.")
                    Toast.makeText(this@LogIncomeExpense, "Invalid budget! Please create or select a valid budget.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                try {
                    if (toggleButton.isChecked) {
                        val totalExpense = expensesDao.getTotalSpentInCategory(userId, category, budgetId, System.currentTimeMillis()) ?: 0f
                        val newExpenseAmount = totalExpense - amount

                        val income = Income(
                            amount = amount,
                            description = description,
                            category = category,
                            date = timestamp,
                            transaction_type = transactionType,
                            imagePath = safeImagePath,
                            user_id = userId,
                            budgetId = budgetId
                        )
                        incomeDao.insertIncome(income)
                        expensesDao.updateExpenseAmount(userId, category, newExpenseAmount, budgetId)
                    } else {
                        val expense = Expenses(
                            amount = amount,
                            description = description,
                            category = category,
                            date = timestamp,
                            transaction_type = transactionType,
                            imagePath = safeImagePath,
                            user_id = userId,
                            budgetId = budgetId
                        )
                        expensesDao.insertExpense(expense)
                    }
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to insert transaction: ${e.message}")
                    Toast.makeText(this@LogIncomeExpense, "Error logging transaction. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }

            Toast.makeText(this@LogIncomeExpense, "Transaction logged successfully!", Toast.LENGTH_SHORT).show()
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

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            val txtSelectedDate = findViewById<TextView>(R.id.txtSelectedDate)
            txtSelectedDate.text = "Selected Date: $selectedDate"

            selectedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val txtSelectedTime = findViewById<TextView>(R.id.txtSelectedTime)
            txtSelectedTime.text = "Selected Time: $selectedTime"
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
            if (imageUri == null) {
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show()
                return
            }

            findViewById<ImageView>(R.id.imgSelectedImage).setImageURI(imageUri)
            imagePath = imageUri.toString()

            // ✅ Persist URI permission for future use
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUri, takeFlags)
            } catch (e: SecurityException) {
                Log.e("IMAGE_ERROR", "Error persisting URI permissions: ${e.message}")
            }
        }
    }

    // 🔥 Show dialog if no categories exist
    private fun showNoCategoriesDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Categories Found")
            .setMessage("You have yet to create a budget. Would you like to create a budget now to link to an expense/income?")
            .setPositiveButton("Yes") { dialog, _ ->
                val intent = Intent(this, CreateBudget::class.java) // ⬅️ Make sure CreateBudget is your correct page
                startActivity(intent)
                finish() // Optional: close this page so they come back clean
            }
            .setNegativeButton("No") { dialog, _ ->
                Toast.makeText(this, "You must create a budget before logging a transaction!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(false) // They MUST choose yes or no
            .show()
    }

    fun adjustTimestamp(originalTimestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = originalTimestamp

        // Subtract one month
        calendar.add(Calendar.MONTH, -1)

        return calendar.timeInMillis
    }
}

