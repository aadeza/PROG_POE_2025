package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class ProfileSettings : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var doneBtn: Button
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "ProfileSettings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        name = findViewById(R.id.edtName)
        surname = findViewById(R.id.edtSurname)
        email = findViewById(R.id.edtEmailAddress)
        phone = findViewById(R.id.edtPhoneNum)
        password = findViewById(R.id.edtRegPassword)
        confirmPassword = findViewById(R.id.edtConfirmPassword)
        doneBtn = findViewById(R.id.btnDoneReg)
        progressBar = findViewById(R.id.progressBar)

        // Disable the email field
        email.isEnabled = false
        email.isFocusable = false
        email.setTextColor(resources.getColor(android.R.color.darker_gray))

        val user_id = SessionManager.getUserId(applicationContext)
        if (!user_id.isNullOrBlank()) {
            db.collection("users").document(user_id).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name.setText(document.getString("name") ?: "")
                        surname.setText(document.getString("surname") ?: "")
                        email.setText(document.getString("email") ?: "")
                        phone.setText(document.getString("number") ?: "")
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Firestore error: ", e)
                }
        }

        doneBtn.setOnClickListener {
            val newName = name.text.toString().trim()
            val newSurname = surname.text.toString().trim()
            val newPhone = phone.text.toString().trim()
            val newPassword = password.text.toString().trim()
            val confirmPasswordText = confirmPassword.text.toString().trim()
            val user = auth.currentUser

            if (user != null) {
                doneBtn.isEnabled = false
                progressBar.visibility = View.VISIBLE

                updateEmailAndPassword(
                    user,
                    newPassword,
                    confirmPasswordText
                ) {
                    updateFirestoreUser(newName, newSurname, newPhone)
                }
            }
        }
    }

    private fun updateEmailAndPassword(
        user: FirebaseUser,
        newPassword: String,
        confirmPassword: String,
        onComplete: () -> Unit
    ) {
        if (newPassword.isNotEmpty()) {
            updatePassword(user, newPassword, confirmPassword, onComplete)
        } else {
            onComplete()
        }
    }

    private fun updatePassword(
        user: FirebaseUser,
        newPassword: String,
        confirmPassword: String,
        onComplete: () -> Unit
    ) {
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            doneBtn.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }

        user.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Password update failed", task.exception)
                }
                onComplete()
            }
    }

    private fun updateFirestoreUser(name: String, surname: String, phone: String) {
        val user_id = SessionManager.getUserId(applicationContext)
        if (user_id.isNullOrBlank()) {
            Toast.makeText(this, "User ID not found. Cannot save settings.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "User ID is null or blank. Cannot save.")
            doneBtn.isEnabled = true
            progressBar.visibility = View.GONE
            return
        }

        val updates = mapOf(
            "name" to name,
            "surname" to surname,
            "number" to phone,
            "user_id" to user_id
        )

        db.collection("users").document(user_id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Firestore profile updated: $updates")
                doneBtn.isEnabled = true
                progressBar.visibility = View.GONE
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Firestore update failed", e)
                doneBtn.isEnabled = true
                progressBar.visibility = View.GONE
            }
    }
}
/**
 * Source: https://www.geeksforgeeks.org/implementing-edit-profile-data-functionality-in-social-media-android-app/
 * Author: GeeksforGeeks
 * License: CC BY-SA 4.0 (https://creativecommons.org/licenses/by-sa/4.0/)
 * Adapted by: Reaobaka Ntoagae for Pennywise
 * Purpose: Enables users to edit their profile information, including name, email, and profile picture
 * Modifications:
 * - Converted original Java code to Kotlin
 * - Integrated with Firebase Firestore for real-time data updates
 * - Enhanced UI/UX with Material Design components
 */

