package com.example.prog_poe_2025

import Data_Classes.Notification
import Data_Classes.Users
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val doneRegButton = findViewById<Button>(R.id.btnDoneReg)
        doneRegButton.setOnClickListener {
            val name = findViewById<EditText>(R.id.edtName).text.toString()
            val surname = findViewById<EditText>(R.id.edtSurname).text.toString()
            val email = findViewById<EditText>(R.id.edtEmailAddress).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.edtConfirmPassword).text.toString()
            val password = findViewById<EditText>(R.id.edtRegPassword).text.toString()
            val number = findViewById<EditText>(R.id.edtPhoneNum).text.toString()

            // Ensure all fields are filled
            if (name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && number.isNotEmpty()) {

                // Check that name and surname contain no digits
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

                val hashedPassword = PasswordUtils.hashPassword(password)
                val user = Users(name = name, surname = surname, email = email, password = hashedPassword, number = number)

                lifecycleScope.launch {
                    try {
                        val db = AppDatabase.getDatabase(applicationContext)
                        val userDao = db.userDao()

                        userDao.insertUser(user)

                        runOnUiThread {
                            Toast.makeText(this@Register, "User successfully registered!", Toast.LENGTH_SHORT).show()
                        }

                        val intent = Intent(this@Register, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        Toast.makeText(this@Register, "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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