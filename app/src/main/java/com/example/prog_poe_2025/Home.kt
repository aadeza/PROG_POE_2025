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

        //Link to Create Budget
        val createbudget = findViewById<Button>(R.id.btnCreateBudget)
        createbudget.setOnClickListener(){
            val intent = Intent(this, CreateBudget::class.java)
            startActivity(intent)
        }

        //Link to generate report
        val toGenReport = findViewById<Button>(R.id.btnGenerateReport)
        toGenReport.setOnClickListener(){
            val intent = Intent(this, GenerateReport::class.java)
            startActivity(intent)
        }

        //Link to view budgets
        val toViewBudgets = findViewById<Button>(R.id.btnViewBudgets)
        toViewBudgets.setOnClickListener(){
            val intent = Intent(this, ViewBudgets::class.java)
            startActivity(intent)
        }

        //Link to budget quiz
        val toBudgetQuiz = findViewById<Button>(R.id.btnBudgetQuiz)
        toBudgetQuiz.setOnClickListener(){
            val intent = Intent(this, BudgetQuiz::class.java)
            startActivity(intent)
        }

    }
}