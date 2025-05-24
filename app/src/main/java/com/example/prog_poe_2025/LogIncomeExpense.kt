package com.example.prog_poe_2025

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class LogIncomeExpense : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private lateinit var edtAmount: EditText
    private lateinit var edtDescription: EditText
    private lateinit var spnCategory: Spinner
    private lateinit var spnTransactType: Spinner
    private lateinit var btnLogDate: Button
    private lateinit var btnLogDone: Button
    private lateinit var toggleButton: ToggleButton
    private lateinit var recyclerTransactions: RecyclerView
    private lateinit var imgSelectedImage: ImageView
    private lateinit var txtSelectedDate: TextView
    private lateinit var txtSelectedTime: TextView


    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var imageUri: Uri? = null
    private var currentUserId: String? = null
    private var selectedBudgetId: String? = null

    private lateinit var categoryNames: MutableList<String>
    private lateinit var transactionAdapter: TransactionAdapter

    private val TAG = "LogIncomeExpense"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_income_expense)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentUserId = SessionManager.getUserId(applicationContext)

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Initialize UI elements
        edtAmount = findViewById(R.id.edtName)
        edtDescription = findViewById(R.id.edtTxtMlDescription)
        spnCategory = findViewById(R.id.spnCategory)
        spnTransactType = findViewById(R.id.spnTransactType)
        btnLogDate = findViewById(R.id.btnLogDate)
        btnLogDone = findViewById(R.id.btnLogDone)
        toggleButton = findViewById(R.id.tgbtnPickIncExp)
        recyclerTransactions = findViewById(R.id.recyclerTransactions)
        imgSelectedImage = findViewById(R.id.imgSelectedImage)
        txtSelectedDate = findViewById(R.id.txtSelectedDate)
        txtSelectedTime = findViewById(R.id.txtSelectedTime)

        // RecyclerView setup
        recyclerTransactions.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        recyclerTransactions.adapter = transactionAdapter

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            fetchTransactions(isChecked)
        }
        fetchTransactions(toggleButton.isChecked) // initial fetch

        // Setup transaction type spinner
        val transactionTypes = resources.getStringArray(R.array.transaction_types)
        val transactTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionTypes)
        transactTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnTransactType.adapter = transactTypeAdapter

        // Fetch categories from Firestore
        categoryNames = mutableListOf()
        fetchCategories()

        btnLogDate.setOnClickListener { showDatePicker() }

        btnLogDone.setOnClickListener {
            saveTransaction()
        }

        // Bottom navigation setup (unchanged)
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

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                imageUri = result.data?.data
                imgSelectedImage.setImageURI(imageUri)
                Log.d(TAG, "Image selected: $imageUri")
            }
        }
        imgSelectedImage.setOnClickListener {
            Log.d(TAG, "Image button clicked!") // ✅ Debugging
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent) // ✅ Use Activity Result API
        }
        val retrievedBudgetId = SessionManager.getSelectedBudgetId(applicationContext)
        Log.d(TAG, "SessionManager stored budget ID: $retrievedBudgetId") // ✅ Check what it returns

        selectedBudgetId = retrievedBudgetId ?: ""
        Log.d(TAG, "Assigned selected budget ID: $selectedBudgetId") // ✅ Confirm it's set properly
    }

    private fun fetchCategories() {
        if (currentUserId == null) return

        firestore.collection("budgets")
            .whereEqualTo("user_id", currentUserId)
            .get()
            .addOnSuccessListener { budgetSnapshot ->
                if (budgetSnapshot.isEmpty) {
                    showNoCategoriesDialog() // No budgets exist for this user.
                    return@addOnSuccessListener
                }

                val categoryIds = mutableSetOf<String>()

                for (budgetDoc in budgetSnapshot.documents) {
                    val budgetCategories = budgetDoc.get("categories") as? List<String> ?: emptyList()
                    categoryIds.addAll(budgetCategories) // Merge categories from all budgets
                }

                if (categoryIds.isEmpty()) {
                    showNoCategoriesDialog() // Budgets exist but have no categories.
                    return@addOnSuccessListener
                }

                // Fetch category names based on collected IDs
                categoryNames.clear()

                firestore.collection("categories").whereIn(FieldPath.documentId(), categoryIds.toList())
                    .get()
                    .addOnSuccessListener { categorySnapshot ->
                        categoryNames.addAll(categorySnapshot.documents.mapNotNull { doc ->
                            doc.getString("name")
                        })

                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnCategory.adapter = adapter
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error loading categories", e)
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching budgets", e)
                Toast.makeText(this, "Error fetching budgets.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTransactions(isExpense: Boolean) {
        if (currentUserId == null) return

        val collection = if (isExpense) "expenses" else "incomes"

        firestore.collection(collection) // 🔹 Querying global collections directly
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "Fetched transactions from $collection: ${snapshot.documents.map { it.id }}")

                val transactions = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    val isExpenseType = collection == "expenses"

                    if (isExpenseType) {
                        Expense(id, amount, date)
                    } else {
                        Income(id, amount, date)
                    }
                }

                transactionAdapter.updateTransactions(transactions)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading $collection", e)
                Toast.makeText(this, "Error loading transactions.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveTransaction() {
        val amountText = edtAmount.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val categoryName = spnCategory.selectedItem?.toString() ?: ""

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (amountText.isEmpty() || selectedDate.isNullOrEmpty() || selectedTime.isNullOrEmpty() || categoryName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Enter a valid amount.", Toast.LENGTH_SHORT).show()
            return
        }

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fullDateTime = "$selectedDate $selectedTime"
        val timestamp = try {
            formatter.parse(fullDateTime)?.time ?: System.currentTimeMillis()
        } catch (ex: Exception) {
            Log.e(TAG, "Error parsing date", ex)
            Toast.makeText(this, "Invalid date/time. Please select again.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                var imageUrl: String? = null
                if (imageUri != null) {
                    Toast.makeText(this@LogIncomeExpense, "Uploading image...", Toast.LENGTH_SHORT).show()
                    val imgRef = storage.reference.child("images/$currentUserId/${UUID.randomUUID()}.jpg")
                    val uploadTask = imgRef.putFile(imageUri!!)
                    imageUrl = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) task.exception?.let { throw it }
                        imgRef.downloadUrl
                    }.await().toString()
                    Log.d(TAG, "Image uploaded to $imageUrl")
                }

                // Step 1: Retrieve the category document ID from the "categories" collection
                val categoryQuerySnapshot = firestore.collection("categories")
                    .whereEqualTo("name", categoryName)
                    .limit(1)
                    .get()
                    .await()

                if (categoryQuerySnapshot.isEmpty) {
                    Toast.makeText(this@LogIncomeExpense, "Category not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val categoryId = categoryQuerySnapshot.documents[0].id
                Log.d(TAG, "Fetched categoryId for \"$categoryName\": $categoryId")

                // Step 2: Query budgets for the current user whose "categories" array contains this categoryId.
                val budgetsQuerySnapshot = firestore.collection("budgets")
                    .whereEqualTo("user_id", currentUserId)
                    .whereArrayContains("categories", categoryId)
                    .get()
                    .await()

                if (budgetsQuerySnapshot.isEmpty) {
                    Toast.makeText(
                        this@LogIncomeExpense,
                        "Selected category does not exist in any budget.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // 🔹 **Fix: Use the toggle button to determine Expense or Income**
                val collection = if (toggleButton.isChecked) "incomes" else "expenses"

                // Step 3: Save transaction to all applicable budgets
                for (budgetDoc in budgetsQuerySnapshot.documents) {
                    val assignedBudgetId = budgetDoc.id
                    Log.d(TAG, "Assigning transaction to budget ID: $assignedBudgetId")

                    val transactionData = hashMapOf(
                        "amount" to amount,
                        "categoryId" to categoryId,
                        "categoryName" to categoryName,
                        "date" to timestamp,
                        "description" to description,
                        "imageUrl" to imageUrl,
                        "userId" to currentUserId,
                        "budgetId" to assignedBudgetId
                    )

                    Log.d(TAG, "Saving transaction: $transactionData")

                    firestore.collection(collection)
                        .add(transactionData)
                        .await()
                }

                Toast.makeText(this@LogIncomeExpense, "Transaction logged successfully in all applicable budgets.", Toast.LENGTH_SHORT).show()

                // Clear inputs after successful save
                edtAmount.text.clear()
                edtDescription.text.clear()
                imageUri = null
                imgSelectedImage.setImageResource(R.drawable.noimage)
                txtSelectedDate.text = "Date:"
                txtSelectedTime.text = "Time:"
                selectedDate = null
                selectedTime = null

                fetchTransactions(toggleButton.isChecked) // Refresh the transaction list

            } catch (e: Exception) {
                Log.e(TAG, "Error saving transaction", e)
                Toast.makeText(this@LogIncomeExpense, "Failed to save transaction.", Toast.LENGTH_SHORT).show()
            }
        }
    }

private fun showDatePicker() {
    val c = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        this,
        { _, year, month, dayOfMonth ->
            selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            txtSelectedDate.text = "Date: $selectedDate"
            // Also set time picker here or keep separate?
            showTimePicker()
        },
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.show()
}

private fun showTimePicker() {
    val c = Calendar.getInstance()
    val timePickerDialog = android.app.TimePickerDialog(
        this,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
            txtSelectedTime.text = "Time: $selectedTime"
        },
        c.get(Calendar.HOUR_OF_DAY),
        c.get(Calendar.MINUTE),
        true
    )
    timePickerDialog.show()
}

private fun showNoCategoriesDialog() {
    val dialog = android.app.AlertDialog.Builder(this)
        .setTitle("No Categories Found")
        .setMessage("You have no categories. Please add a category first.")
        .setPositiveButton("Go to Categories") { _, _ ->
            startActivity(Intent(this, CreateBudget::class.java))
        }
        .setNegativeButton("Cancel", null)
        .create()
    dialog.show()
}
}


///(W3Schools,2025)

/*Reference List
W3Schools, 2025. Kotlin Tutorial, n.d. [Online]. Available at:
https://www.w3schools.com/kotlin/index.php [Accessed 19 April 2025].
*/

