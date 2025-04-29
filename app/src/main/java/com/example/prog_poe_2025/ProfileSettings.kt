package com.example.prog_poe_2025


import DAOs.UserDao
import Data_Classes.Expenses
import Data_Classes.Income
import Data_Classes.Users
import android.app.DatePickerDialog
import android.content.Context
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

class ProfileSettings : AppCompatActivity() {

    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var imgProfilePicture: ImageView
    private lateinit var btnSaveChanges: Button
    private lateinit var btnLogout: Button
    private lateinit var btnChangePassword: Button
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private var currentUser: Users? = null

    private val IMAGE_PICK_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_settings_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnChangePassword = findViewById(R.id.btnChangePassword)
        edtUsername = findViewById(R.id.edtUsername)
        edtEmail = findViewById(R.id.edtEmail)
        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnLogout = findViewById(R.id.btnLogout)

        database = AppDatabase.getDatabase(applicationContext)
        userDao = database.userDao()


        loadUserProfile()

        imgProfilePicture.setOnClickListener {
            openGallery()
        }

        btnSaveChanges.setOnClickListener {
            saveProfileChanges()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadUserProfile() {
        val email = SessionManager.getEmail(this)  // get current logged in email

        lifecycleScope.launch {
            currentUser = userDao.getUserByEmail(email ?: "")
            currentUser?.let { user ->
                runOnUiThread {
                    edtUsername.setText(user.name)
                    edtEmail.setText(user.email)
                    user.profileImageUri?.let {
                        imgProfilePicture.setImageURI(Uri.parse(it))
                    }
                }
            }
        }
    }


    private fun saveProfileChanges() {
        val newUsername = edtUsername.text.toString()
        val newEmail = edtEmail.text.toString()

        if (newUsername.isNotBlank() && newEmail.isNotBlank()) {
            currentUser?.let { user ->
                val updatedUser = user.copy(
                    name = newUsername,
                    email = newEmail
                )

                lifecycleScope.launch {
                    userDao.updateUser(updatedUser)
                    SessionManager.saveUsername(this@ProfileSettings, newUsername)
                    SessionManager.saveEmail(this@ProfileSettings, newEmail)

                    runOnUiThread {
                        Toast.makeText(this@ProfileSettings, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data

            imageUri?.let {
                imgProfilePicture.setImageURI(it)
                currentUser?.let { user ->
                    val updatedUser = user.copy(profileImageUri = it.toString())

                    lifecycleScope.launch {
                        userDao.updateUser(updatedUser)
                    }
                }
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = dialogView.findViewById<EditText>(R.id.currentPassword)
        val newPasswordInput = dialogView.findViewById<EditText>(R.id.newPassword)
        val confirmNewPasswordInput = dialogView.findViewById<EditText>(R.id.confirmNewPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmNewPasswordInput.text.toString()

                validateAndChangePassword(currentPassword, newPassword, confirmPassword)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun validateAndChangePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentUser?.password != currentPassword) {
            Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            val updatedUser = user.copy(password = newPassword)

            lifecycleScope.launch {
                userDao.updateUser(updatedUser)

                runOnUiThread {
                    Toast.makeText(this@ProfileSettings, "Password changed successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logoutUser() {
        SessionManager.clearSession(this)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

