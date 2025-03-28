package com.example.prog_poe_2025

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        //Link to LogIncExp
        val toLogIncExp = findViewById<Button>(R.id.btnLogExpInc)
        toLogIncExp.setOnClickListener(){
            val intent = Intent(this, LogIncomeExpense::class.java)
            startActivity(intent)
        }

        //Link to Create Budget
        val createbudget = findViewById<Button>(R.id.btnCreateBudget)
        createbudget.setOnClickListener(){
            val intent = Intent(this, CreateBudget::class.java)
            startActivity(intent)
        }
    }
}