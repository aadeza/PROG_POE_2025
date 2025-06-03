package com.example.prog_poe_2025

import android.Manifest
import com.google.firebase.storage.StorageReference
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
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

    private lateinit var imgLog: ImageButton
    private lateinit var imgSelectedImage: ImageView
    private lateinit var txtSelectedDate: TextView
    private lateinit var txtSelectedTime: TextView

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var imageUri: Uri? = null
    private var currentUserId: String? = null

    private lateinit var categoryNames: MutableList<String>

    // Use a single TAG for date parsing errors:
    private val TAG_DATE = "LogIncExp_DATE"
    private val TAG_UPLOAD = "LogIncExp_UPLOAD"
    private val TAG_SAVE = "LogIncExp_SAVE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_income_expense)

        // Apply edge-to-edge padding
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

        imgLog = findViewById(R.id.imgLog)                   // <-- pick image on this button
        imgSelectedImage = findViewById(R.id.imgSelectedImage)
        txtSelectedDate = findViewById(R.id.txtSelectedDate)
        txtSelectedTime = findViewById(R.id.txtSelectedTime)

        // Setup transaction type spinner
        val transactionTypes = resources.getStringArray(R.array.transaction_types)
        val transactTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transactionTypes)
        transactTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnTransactType.adapter = transactTypeAdapter

        // Fetch categories from Firestore
        categoryNames = mutableListOf()
        fetchCategories()

        btnLogDate.setOnClickListener { showDatePicker() }
        btnLogDone.setOnClickListener { saveTransaction() }

        // Bottom navigation setup
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

        requestStoragePermission()
        // Get instance of Firebase Storage
        val storage = FirebaseStorage.getInstance()

        // Reference to your default bucket
        val storageRef: StorageReference = storage.reference

        // Optional: reference to folder/path inside bucket
        val imagesRef = storageRef.child("images")



        // Register image picker for "imgLog" button clicks
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                imageUri = result.data!!.data
                imgSelectedImage.setImageURI(imageUri)
                Log.d(TAG_UPLOAD, "User selected image URI = $imageUri")
            }
        }
        imgLog.setOnClickListener {
            // Launch gallery picker when the user clicks the "imgLog" ImageButton
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }
    }

    private fun fetchCategories() {
        if (currentUserId == null) return

        firestore.collection("budgets")
            .whereEqualTo("user_id", currentUserId)
            .get()
            .addOnSuccessListener { budgetSnapshot ->
                if (budgetSnapshot.isEmpty) {
                    showNoCategoriesDialog()
                    return@addOnSuccessListener
                }

                val categoryIds = mutableSetOf<String>()
                for (budgetDoc in budgetSnapshot.documents) {
                    val budgetCategories = budgetDoc.get("categories") as? List<String> ?: emptyList()
                    categoryIds.addAll(budgetCategories)
                }
                if (categoryIds.isEmpty()) {
                    showNoCategoriesDialog()
                    return@addOnSuccessListener
                }

                categoryNames.clear()
                firestore.collection("categories")
                    .whereIn(FieldPath.documentId(), categoryIds.toList())
                    .get()
                    .addOnSuccessListener { categorySnapshot ->
                        categoryNames.addAll(categorySnapshot.documents.mapNotNull { it.getString("name") })
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnCategory.adapter = adapter
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching budgets.", Toast.LENGTH_SHORT).show()
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

        // Parse date/time to timestamp
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fullDateTime = "$selectedDate $selectedTime"
        val timestamp = try {
            formatter.parse(fullDateTime)?.time ?: System.currentTimeMillis()
        } catch (ex: Exception) {
            Log.e(TAG_DATE, "Error parsing date/time:", ex)
            Toast.makeText(this, "Invalid date/time. Please select again.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1) Upload image if one was picked
                var imageUrl: String? = null
                if (imageUri != null) {
                    Toast.makeText(this@LogIncomeExpense, "Uploading image...", Toast.LENGTH_SHORT).show()

                    // Log which bucket we got from FirebaseStorage
                    val bucketName = storage.reference.bucket
                    Log.d(TAG_UPLOAD, "Firebase Storage bucket = $bucketName")

                    // Build a storage reference under /images/{userId}/{UUID}.jpg
                    val uniqueName = UUID.randomUUID().toString() + ".jpg"
                    val imgRef = storage.reference
                        .child("images/$currentUserId/$uniqueName")

                    // Log the full path we’re about to use
                    Log.d(TAG_UPLOAD, "Attempting upload to path = ${imgRef.path}")

                    // 1a) Perform the upload
                    imgRef.putFile(imageUri!!).await()
                    Log.d(TAG_UPLOAD, "putFile(...) succeeded for URI = $imageUri")

                    // 1b) Then fetch the download URL
                    imageUrl = imgRef.downloadUrl.await().toString()
                    Log.d(TAG_UPLOAD, "downloadUrl() = $imageUrl")
                } else {
                    Log.d(TAG_UPLOAD, "No image selected; skipping upload")
                }

                // 2) Find categoryId for categoryName
                val categoryQuery = firestore.collection("categories")
                    .whereEqualTo("name", categoryName)
                    .limit(1)
                    .get()
                    .await()

                if (categoryQuery.isEmpty) {
                    Toast.makeText(this@LogIncomeExpense, "Category not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val categoryId = categoryQuery.documents[0].id
                Log.d(TAG_SAVE, "Found categoryId = $categoryId for name \"$categoryName\"")

                // 3) Find budgets that include this categoryId
                val budgetsSnapshot = firestore.collection("budgets")
                    .whereEqualTo("user_id", currentUserId)
                    .whereArrayContains("categories", categoryId)
                    .get()
                    .await()

                if (budgetsSnapshot.isEmpty) {
                    Toast.makeText(
                        this@LogIncomeExpense,
                        "Selected category not in any budget.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // 4) Determine "incomes" vs "expenses"
                val collectionName = if (toggleButton.isChecked) "incomes" else "expenses"
                Log.d(TAG_SAVE, "Saving into collection \"$collectionName\"")

                // 5) Insert a document for each matching budget
                for (budgetDoc in budgetsSnapshot.documents) {
                    val assignedBudgetId = budgetDoc.id
                    Log.d(TAG_SAVE, "Assigning to budget ID: $assignedBudgetId")

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

                    Log.d(TAG_SAVE, "Adding Firestore doc: $transactionData")
                    firestore.collection(collectionName)
                        .add(transactionData)
                        .await()
                }

                Toast.makeText(
                    this@LogIncomeExpense,
                    "Transaction logged successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                // 6) Clear form
                edtAmount.text.clear()
                edtDescription.text.clear()
                imageUri = null
                imgSelectedImage.setImageResource(R.drawable.noimage)
                txtSelectedDate.text = "Date:"
                txtSelectedTime.text = "Time:"
                selectedDate = null
                selectedTime = null

            } catch (e: Exception) {
                Log.e(TAG_SAVE, "Error saving transaction:", e)
                Toast.makeText(this@LogIncomeExpense, "Failed to save transaction.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                txtSelectedDate.text = "Date: $selectedDate"
                showTimePicker()
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val c = Calendar.getInstance()
        android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime = String.format("%02d:%02d:00", hourOfDay, minute)
                txtSelectedTime.text = "Time: $selectedTime"
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        ).show()
    }
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    1001
                )
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1001
                )
            }
        }
    }


    private fun showNoCategoriesDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("No Categories Found")
            .setMessage("You have no categories. Please add a category first.")
            .setPositiveButton("Go to Categories") { _, _ ->
                startActivity(Intent(this, CreateBudget::class.java))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}


///(W3Schools,2025)

/*Reference List
W3Schools, 2025. Kotlin Tutorial, n.d. [Online]. Available at:
https://www.w3schools.com/kotlin/index.php [Accessed 19 April 2025].
*/

