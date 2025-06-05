package com.example.prog_poe_2025

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class QuizQuestion(
    val text: String = "",
    val option1: String = "",
    val option2: String = "",
    val option3: String = "",
    val correctAnswer: String = ""
)

class BudgetQuiz : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnNext: Button
    private lateinit var txtTimer: TextView
    private lateinit var btnStartQuiz: Button
    private lateinit var txtQuizHeading: TextView
    private lateinit var txtIntro: TextView
    private lateinit var txtSub: TextView
    private lateinit var progressBar: ProgressBar

    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer: String? = null
    private var timer: CountDownTimer? = null
    private var quizStartTime: Long = 0L

    private var quizQuestions: List<QuizQuestion> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_quiz)

        txtQuestion = findViewById(R.id.txtQuestion)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)
        btnNext = findViewById(R.id.btnNext)
        txtTimer = findViewById(R.id.txtTimer)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        txtQuizHeading = findViewById(R.id.txtQuizHeading)
        txtIntro = findViewById(R.id.txtIntro)
        txtSub = findViewById(R.id.txtSub)
        progressBar = findViewById(R.id.quizProgressBar)

        progressBar.max = 10
        progressBar.progress = 0

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_game
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_transaction -> {
                    startActivity(Intent(this, LogIncomeExpense::class.java))
                    true
                }
                R.id.nav_viewBudgets -> {
                    startActivity(Intent(this, ViewBudgets::class.java))
                    true
                }
                R.id.nav_game -> true
                else -> false
            }
        }

        btnStartQuiz.setOnClickListener {
            loadQuizQuestions()
            startSpinningTitle()
            startBackgroundColorAnimation()
            startQuiz()
        }

        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener {
            if (selectedAnswer == null) {
                Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show()
            } else {
                nextQuestion()
            }
        }
    }

    private fun loadQuizQuestions() {
        quizQuestions = getSampleQuestions().shuffled().take(10)
        if (quizQuestions.isNotEmpty()) {
            progressBar.max = quizQuestions.size
        } else {
            txtQuestion.text = "No quiz questions available."
        }
    }

    private fun getSampleQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion("What is the purpose of budgeting?", "To spend more", "To manage income", "To avoid planning", "To manage income"),
            QuizQuestion("Which is a fixed expense?", "Rent", "Groceries", "Dining out", "Rent"),
            QuizQuestion("Why is saving important?", "For emergencies", "To spend more", "To waste money", "For emergencies"),
            QuizQuestion("What tracks your spending?", "Calendar", "Expense Tracker", "Weather App", "Expense Tracker"),
            QuizQuestion("First step in budgeting?", "Spend first", "Record income", "Borrow money", "Record income"),
            QuizQuestion("What is variable expense?", "Internet bill", "Electricity", "Clothing", "Clothing"),
            QuizQuestion("Good budgeting habit?", "Impulse buying", "Tracking expenses", "Ignoring bills", "Tracking expenses"),
            QuizQuestion("What is net income?", "Salary before tax", "Income after tax", "Gross income", "Income after tax"),
            QuizQuestion("Budgeting helps to?", "Spend freely", "Avoid debt", "Ignore expenses", "Avoid debt"),
            QuizQuestion("Unexpected cost is called?", "Fixed expense", "Variable", "Emergency expense", "Emergency expense"),
            QuizQuestion("Where to keep your budget?", "Head", "Notebook", "Anywhere safe", "Anywhere safe"),
            QuizQuestion("Which is NOT a budget tool?", "Spreadsheet", "Bank app", "Gaming app", "Gaming app"),
            QuizQuestion("When should you update a budget?", "Monthly", "Yearly", "Never", "Monthly"),
            QuizQuestion("Income higher than expenses means?", "Deficit", "Break even", "Surplus", "Surplus"),
            QuizQuestion("Which helps lower spending?", "Avoiding sales", "Impulse shopping", "Tracking expenses", "Tracking expenses"),
            QuizQuestion("Best reason to use a budget app?", "To forget due dates", "To track finance", "To lose money", "To track finance"),
            QuizQuestion("Which is a budgeting goal?", "More debt", "Overspending", "Saving for a trip", "Saving for a trip"),
            QuizQuestion("What is a financial goal?", "Watch movies", "Pay debt", "Eat out often", "Pay debt"),
            QuizQuestion("What increases savings?", "Spending more", "Earning more", "Budgeting", "Budgeting"),
            QuizQuestion("Best way to reduce debt?", "Ignore it", "Pay minimum", "Make regular payments", "Make regular payments"),
            QuizQuestion("How to prepare for emergencies?", "Spend all income", "Save a portion", "Take loans", "Save a portion"),
            QuizQuestion("What is a want?", "Water bill", "Medical aid", "New shoes", "New shoes"),
            QuizQuestion("What is a need?", "Concert ticket", "Electricity", "Holiday trip", "Electricity"),
            QuizQuestion("Late payment causes?", "Rewards", "Debt", "Savings", "Debt"),
            QuizQuestion("Which affects credit score?", "Paying bills late", "Saving money", "Budgeting", "Paying bills late"),
            QuizQuestion("Why review your budget?", "To forget bills", "To keep it updated", "To overspend", "To keep it updated"),
            QuizQuestion("What’s the 50/30/20 rule?", "All for fun", "Needs/Savings/Wants", "Spend/Spend/Spend", "Needs/Savings/Wants"),
            QuizQuestion("Which is a saving strategy?", "Buy more", "Set goals", "Spend blindly", "Set goals"),
            QuizQuestion("Which app helps budgeting?", "Instagram", "Buxfer", "TikTok", "Buxfer"),
            QuizQuestion("What does budgeting prevent?", "Overspending", "Saving", "Working", "Overspending")
        )
    }

    private fun startBackgroundColorAnimation() {
        val colorFrom = Color.parseColor("#6388B4")
        val colorTo = Color.parseColor("#E7C6FF")
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 5000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                txtIntro.setBackgroundColor(animator.animatedValue as Int)
            }
        }
        colorAnimation.start()
    }

    private fun startSpinningTitle() {
        val flip = ObjectAnimator.ofFloat(txtQuizHeading, "rotationY", 0f, 360f).apply {
            duration = 2500
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        flip.start()
    }

    private fun startQuiz() {
        quizStartTime = System.currentTimeMillis()

        btnStartQuiz.visibility = View.GONE
        txtQuestion.visibility = View.VISIBLE
        btnOption1.visibility = View.VISIBLE
        btnOption2.visibility = View.VISIBLE
        btnOption3.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        txtTimer.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        txtSub.visibility = View.GONE

        startEmojiCountdown()
        if (quizQuestions.isNotEmpty()) {
            currentQuestionIndex = 0
            score = 0
            progressBar.progress = 1
            showQuestion()
        } else {
            txtQuestion.text = "No quiz questions available."
        }
    }

    private fun resetAnswerButtons() {
        val defaultColor = ContextCompat.getColor(this, R.color.default_button_background)
        btnOption1.setBackgroundColor(defaultColor)
        btnOption2.setBackgroundColor(defaultColor)
        btnOption3.setBackgroundColor(defaultColor)
    }

    private fun showQuestion() {
        val question = quizQuestions[currentQuestionIndex]
        resetAnswerButtons()
        txtQuestion.text = question.text
        btnOption1.text = question.option1
        btnOption2.text = question.option2
        btnOption3.text = question.option3
        selectedAnswer = null
    }

    private fun selectAnswer(answer: String) {
        selectedAnswer = answer
        resetAnswerButtons()
        val selectedColor = ContextCompat.getColor(this, R.color.selected_button_background)
        when (answer) {
            btnOption1.text -> btnOption1.setBackgroundColor(selectedColor)
            btnOption2.text -> btnOption2.setBackgroundColor(selectedColor)
            btnOption3.text -> btnOption3.setBackgroundColor(selectedColor)
        }
    }

    private fun nextQuestion() {
        if (selectedAnswer == quizQuestions[currentQuestionIndex].correctAnswer) {
            score++
        }
        if (currentQuestionIndex < quizQuestions.size - 1) {
            currentQuestionIndex++
            progressBar.progress = currentQuestionIndex + 1
            showQuestion()
        } else {
            endQuiz()
        }
    }

    private fun endQuiz() {
        timer?.cancel()

        val durationMillis = System.currentTimeMillis() - quizStartTime
        val durationSeconds = durationMillis / 1000

        txtQuestion.text = "Quiz Over! Your score: $score/${quizQuestions.size}"
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        btnNext.visibility = View.GONE
        txtTimer.visibility = View.GONE

        saveQuizResultToFirestore(score, durationSeconds)
    }

    /**
     * Source: https://firebase.google.com/docs/firestore/manage-data/add-data
     * Author: Firebase Documentation (Google)
     * License: Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
     * Adapted by: Itumeleng Molawa for Pennywise
     * Purpose: Saves data to Firebase Firestore database
     * Modifications:
     * - Wrapped data saving in Kotlin coroutine for asynchronous operation
     * - Added custom error handling
     */
    private fun saveQuizResultToFirestore(score: Int, durationSeconds: Long) {
        val user = SessionManager.getUserId(applicationContext)

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            val result = hashMapOf(
                "uid" to user,
                "email" to user,
                "score" to score,
                "durationSeconds" to durationSeconds,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("quizResults")
                .add(result)
                .addOnSuccessListener {
                    Toast.makeText(this, "Result saved!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save result: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun startEmojiCountdown() {
        val emojis = listOf("\u23F3", "\uD83D\uDE2C", "\uD83D\uDE31", "\uD83D\uDCA3", "\uD83D\uDD25")
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val emojiIndex = when {
                    secondsLeft > 30 -> 0
                    secondsLeft > 20 -> 1
                    secondsLeft > 10 -> 2
                    secondsLeft > 5 -> 3
                    else -> 4
                }
                txtTimer.text = "Time Left: ${secondsLeft}s ${emojis[emojiIndex]}"
            }

            override fun onFinish() {
                txtTimer.text = "Time's Up! \uD83D\uDCA5"
                endQuiz()
            }
        }.start()
    }
}
