package com.example.prog_poe_2025

import DAOs.UserDao
import Data_Classes.Users
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSettings : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var imgProfilePicture: ImageView
    private lateinit var btnSaveChanges: Button
    private lateinit var btnLogout: Button
    private lateinit var btnChangePassword: Button
    private lateinit var userDao: UserDao
    private var currentUser: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        // Initialize Views
        edtUsername = findViewById(R.id.edtUsername)
        edtEmail = findViewById(R.id.edtEmail)
        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnLogout = findViewById(R.id.btnLogout)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Optionally disable email field if email is not editable
        edtEmail.isEnabled = false

        // Initialize DAO
        val database = AppDatabase.getDatabase(applicationContext)
        userDao = database.userDao()

        // Load User Profile
        loadUserProfile()

        // Save Changes
        btnSaveChanges.setOnClickListener {
            lifecycleScope.launch { saveProfileChanges() }
        }

        // Logout
        btnLogout.setOnClickListener {
            logoutUser()
        }

        // Change Password
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadUserProfile() {
        val email = SessionManager.getEmail(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email ?: "")
            if (user != null) {
                currentUser = user
                runOnUiThread {
                    edtUsername.setText(user.name)
                    edtEmail.setText(user.email)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this@ProfileSettings, "Unable to load user profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun saveProfileChanges() {
        val updatedUsername = edtUsername.text.toString().trim()

        if (updatedUsername.isBlank()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileSettings, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
            return
        }

        withContext(Dispatchers.IO) {
            currentUser?.let { user ->
                val updatedUser = user.copy(name = updatedUsername)
                userDao.updateUser(updatedUser)
                currentUser = updatedUser
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@ProfileSettings, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = view.findViewById<EditText>(R.id.currentPassword)
        val newPasswordInput = view.findViewById<EditText>(R.id.newPassword)
        val confirmPasswordInput = view.findViewById<EditText>(R.id.confirmNewPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(view)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()

                lifecycleScope.launch {
                    validateAndChangePassword(currentPassword, newPassword, confirmPassword)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private suspend fun validateAndChangePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileSettings, "New passwords do not match", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val isValid = withContext(Dispatchers.IO) {
            PasswordUtils.verifyPassword(currentPassword, currentUser?.password?: " ")
        }

        if (!isValid) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ProfileSettings, "Incorrect current password", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val newHashedPassword = withContext(Dispatchers.Default) {
            PasswordUtils.hashPassword(newPassword)
        }

        withContext(Dispatchers.IO) {
            currentUser?.let { user ->
                val updatedUser = user.copy(password = newHashedPassword)
                userDao.updateUser(updatedUser)
                currentUser = updatedUser
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(this@ProfileSettings, "Password changed successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        SessionManager.clearSession(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
