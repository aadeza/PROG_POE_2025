package com.example.prog_poe_2025

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Link to Home
        val login = findViewById<Button>(R.id.btnLogin)
        login.setOnClickListener(){
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }


        // Link to Register
        val registerText = findViewById<TextView>(R.id.txtToRegister)
        registerText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

<<<<<<< Updated upstream
=======

        // Link to Register
        val registerText = findViewById<TextView>(R.id.txtToRegister)
        registerText.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

>>>>>>> Stashed changes
    }
}
