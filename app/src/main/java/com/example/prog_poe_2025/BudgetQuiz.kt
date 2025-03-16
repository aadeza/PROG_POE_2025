package com.example.prog_poe_2025

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BudgetQuiz : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnNext: Button
    private lateinit var txtTimer: TextView
    private lateinit var btnStartQuiz: Button

    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer: String? = null
    private var timer: CountDownTimer? = null

    // Sample quiz questions
    private val quizQuestions = listOf(
        Question("What is budgeting?", "A plan for money", "A shopping list", "A bank account", "A plan for money"),
        Question("Which expense is fixed?", "Rent", "Groceries", "Entertainment", "Rent"),
        Question("What is an emergency fund?", "Money saved for surprises", "A travel fund", "A loan", "Money saved for surprises"),
        // Add 12 more questions
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_quiz)

        // Initialize views
        txtQuestion = findViewById(R.id.txtQuestion)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)
        btnNext = findViewById(R.id.btnNext)
        txtTimer = findViewById(R.id.txtTimer)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)

        // Start quiz when button is clicked
        btnStartQuiz.setOnClickListener {
            startQuiz()
        }

        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }
    }

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

    private fun showQuestion() {
        val question = quizQuestions[currentQuestionIndex]
        txtQuestion.text = question.text
        btnOption1.text = question.option1
        btnOption2.text = question.option2
        btnOption3.text = question.option3
        selectedAnswer = null
    }

    private fun selectAnswer(answer: String) {
        selectedAnswer = answer
    }

    private fun nextQuestion() {
        if (selectedAnswer == quizQuestions[currentQuestionIndex].correctAnswer) {
            score++
        }

        if (currentQuestionIndex < quizQuestions.size - 1) {
            currentQuestionIndex++
            showQuestion()
        } else {
            endQuiz()
        }
    }

    private fun endQuiz() {
        timer?.cancel()
        txtQuestion.text = "Quiz Over! Your score: $score/${quizQuestions.size}"
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        btnNext.visibility = View.GONE
        txtTimer.visibility = View.GONE
    }
}

data class Question(
    val text: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correctAnswer: String
)
