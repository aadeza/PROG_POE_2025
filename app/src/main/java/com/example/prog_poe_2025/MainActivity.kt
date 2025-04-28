package com.example.prog_poe_2025

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val login = findViewById<Button>(R.id.btnLogin)

        login.setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val user = db.userDao().getUserByEmail(email)

                    if (user != null && PasswordUtils.verifyPassword(password, user.password)) {
                        // Save the user ID after successful login
                        SessionManager.saveUserId(this@MainActivity, user.id)


                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MainActivity, Home::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Link to Register
        val registerText = findViewById<TextView>(R.id.txtToRegister)
        registerText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    // Save User ID to SharedPreferences
    private fun saveUserId(context: Context, userId: Int) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("user_id", userId)
        editor.apply()
    }

    // Retrieve User ID from SharedPreferences
    private fun getUserId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("user_id", -1)  // Default value is -1 if no user_id is found
    }
}
