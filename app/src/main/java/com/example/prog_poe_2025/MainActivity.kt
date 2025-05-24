package com.example.prog_poe_2025

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MYDEBUG", "Main Activity started successfully")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val login = findViewById<Button>(R.id.btnLogin)

        login.setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            val userId = firebaseUser?.uid

                            if (userId != null) {
                                // ✅ Save the user ID in SharedPreferences
                                SessionManager.saveUserId(this@MainActivity, userId)

                                Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@MainActivity, Home::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@MainActivity, "Login successful, but user ID not found.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid email or password: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Register link
        val registerText = findViewById<TextView>(R.id.txtToRegister)
        registerText.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }
}

//(W3Schools,2025)

/*Reference List
W3Schools, 2025. Kotlin Tutorial, n.d. [Online]. Available at:
https://www.w3schools.com/kotlin/index.php [Accessed 19 April 2025].
*/

