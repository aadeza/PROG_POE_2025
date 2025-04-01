package com.example.prog_poe_2025

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

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

    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer: String? = null
    private var timer: CountDownTimer? = null

    // Sample quiz questions
    private val quizQuestions = listOf(
        Question("What is budgeting?", "A plan for money", "A shopping list", "A bank account", "A plan for money"),
        Question("Which expense is fixed?", "Rent", "Groceries", "Entertainment", "Rent"),
        Question("What is an emergency fund?", "Money saved for surprises", "A travel fund", "A loan", "Money saved for surprises")
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_quiz)

        // Initialize views
        txtIntro = findViewById(R.id.txtIntro)
        txtQuestion = findViewById(R.id.txtQuestion)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)
        btnNext = findViewById(R.id.btnNext)
        txtTimer = findViewById(R.id.txtTimer)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        txtQuizHeading = findViewById(R.id.txtQuizHeading)

        // Setup BottomNavigationView
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
                R.id.nav_game -> {
                    true // Stay on BudgetQuiz
                }
                else -> false
            }
        }

        // Start quiz when button is clicked
        btnStartQuiz.setOnClickListener { startQuiz() }
        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }

        // Start the spinning animation and background color transition
        startSpinningTitle()
        startBackgroundColorAnimation()
    }

    // Animate the background color transition
    private fun startBackgroundColorAnimation() {
        val colorFrom = Color.parseColor("#00008b")
        val colorTo = Color.parseColor("#800080")
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 5000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                txtIntro.setBackgroundColor(it.animatedValue as Int)
            }
        }
        colorAnimation.start()
    }

    // Make the quiz title spin
    private fun startSpinningTitle() {
        val flip = ObjectAnimator.ofFloat(txtQuizHeading, "rotationY", 0f, 360f).apply {
            duration = 2500
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        flip.start()
    }

    // Start the quiz
    private fun startQuiz() {
        btnStartQuiz.visibility = View.GONE
        txtQuestion.visibility = View.VISIBLE
        btnOption1.visibility = View.VISIBLE
        btnOption2.visibility = View.VISIBLE
        btnOption3.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        txtTimer.visibility = View.VISIBLE

        startTimer()
        showQuestion()
    }

    // Show the current question
    private fun showQuestion() {
        val question = quizQuestions[currentQuestionIndex]
        txtQuestion.text = question.text
        btnOption1.text = question.option1
        btnOption2.text = question.option2
        btnOption3.text = question.option3
        selectedAnswer = null
    }

    // Handle answer selection
    private fun selectAnswer(answer: String) {
        selectedAnswer = answer
    }

    // Move to the next question
    private fun nextQuestion() {
        if (selectedAnswer == quizQuestions[currentQuestionIndex].correctAnswer) score++
        if (currentQuestionIndex < quizQuestions.size - 1) {
            currentQuestionIndex++
            showQuestion()
        } else {
            endQuiz()
        }
    }

    // End the quiz
    private fun endQuiz() {
        timer?.cancel()
        txtQuestion.text = "Quiz Over! Your score: $score/${quizQuestions.size}"
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        btnNext.visibility = View.GONE
        txtTimer.visibility = View.GONE
    }

    // Start the timer
    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtTimer.text = "Time Left: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                txtTimer.text = "Time's Up!"
                endQuiz()
            }
        }.start()
    }
}

// Data class for the quiz questions
data class Question(
    val text: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correctAnswer: String
)


