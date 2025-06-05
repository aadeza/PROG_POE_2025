package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth // Import Firebase Auth
import com.google.firebase.firestore.FirebaseFirestore // Import Firebase Firestore
import com.example.prog_poe_2025.PasswordUtils // Assuming PasswordUtils is in the same package or imported properly


class Register : AppCompatActivity() {

    // Declare Firebase Auth and Firestore instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val doneRegButton = findViewById<Button>(R.id.btnDoneReg)
        doneRegButton.setOnClickListener {
            val name = findViewById<EditText>(R.id.edtName).text.toString().trim() // Trim whitespace
            val surname = findViewById<EditText>(R.id.edtSurname).text.toString().trim() // Trim whitespace
            val email = findViewById<EditText>(R.id.edtEmailAddress).text.toString().trim() // Trim whitespace
            val confirmPassword = findViewById<EditText>(R.id.edtConfirmPassword).text.toString()
            val password = findViewById<EditText>(R.id.edtRegPassword).text.toString()
            val number = findViewById<EditText>(R.id.edtPhoneNum).text.toString().trim() // Trim whitespace

            // --- Validation Checks ---
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.contains(Regex("[0-9]")) || surname.contains(Regex("[0-9]"))) {
                Toast.makeText(this, "Name and Surname cannot contain numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPhoneNumber(number)) {
                Toast.makeText(this, "Please enter a valid South African phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Firebase Registration ---
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User successfully created in Firebase Authentication
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid // Get the unique Firebase User ID

                        if (userId != null) {
                            // Now save additional user details to Firestore
                            val userDetails = hashMapOf(
                                "name" to name,
                                "surname" to surname,
                                "email" to email, // Store email again for easier queries if needed, though Auth also has it
                                "number" to number,

                            )

                            db.collection("users") // Use a collection named "users"
                                .document(userId) // Use the Firebase user ID as the document ID
                                .set(userDetails) // Save the user details
                                .addOnSuccessListener {
                                    Toast.makeText(this, "User registered and details saved!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@Register, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Registration successful, but failed to save details: ${e.message}", Toast.LENGTH_LONG).show()
                                    auth.currentUser?.delete() // Delete the user from Auth if details can't be saved
                                }
                        } else {
                            Toast.makeText(this, "Registration failed: User ID not found.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // If sign-in fails, display a message to the user.
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhoneNumber(number: String): Boolean {
        return number.matches("^0\\d{9}$".toRegex())
    }
}