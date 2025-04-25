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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private lateinit var txtSub : TextView
    private lateinit var progressBar : ProgressBar

    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer: String? = null
    private var timer: CountDownTimer? = null
    private lateinit var questionList: List<Questions>



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_quiz)

        //View Binding
        txtIntro = findViewById(R.id.txtIntro)
        txtQuestion = findViewById(R.id.txtQuestion)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)
        btnNext = findViewById(R.id.btnNext)
        txtTimer = findViewById(R.id.txtTimer)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        txtQuizHeading = findViewById(R.id.txtQuizHeading)
        txtSub = findViewById(R.id.txtSub)
        progressBar = findViewById(R.id.quizProgressBar)

        progressBar.max = 10
        progressBar.progress = 0

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

        // Button Listeners
        btnStartQuiz.setOnClickListener { startQuiz() }
        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }

        val db = AppDatabase.getDatabase(this,lifecycleScope)
        val dao = db.questionsDao()



        // Start the spinning animation and background color transition
        startSpinningTitle()
        startBackgroundColorAnimation()

        lifecycleScope.launch{
            val allQuestions = dao.getAllQuestions()
            questionList = allQuestions.shuffled().take(10)
            showQuestion()
        }
    }

    // Animate the background color transition
    private fun startBackgroundColorAnimation() {
        val colorFrom = Color.parseColor("#6388B4")
        val colorTo = Color.parseColor("#E7C6FF")
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
        progressBar.visibility = View.VISIBLE
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

    // Call this inside showQuestion() before setting the new question
    private fun resetAnswerButtons() {
        val defaultColor = ContextCompat.getColor(this, R.color.default_button_background)

        btnOption1.setBackgroundColor(defaultColor)
        btnOption2.setBackgroundColor(defaultColor)
        btnOption3.setBackgroundColor(defaultColor)

    }

    // Show the current question
    private fun showQuestion() {

        if(::questionList.isInitialized && currentQuestionIndex < questionList.size){
            resetAnswerButtons()
            val question = questionList[currentQuestionIndex]
            txtQuestion.text = question.text
            btnOption1.text = question.option1
            btnOption2.text = question.option2
            btnOption3.text = question.option3
            selectedAnswer = null
        }

    }
    private fun resetOptionStyles() {
        val defaultColor = ContextCompat.getColor(this, R.color.default_button_background)
        btnOption1.setBackgroundColor(defaultColor)
        btnOption2.setBackgroundColor(defaultColor)
        btnOption3.setBackgroundColor(defaultColor)
    }

    // Handle answer selection
    private fun selectAnswer(answer: String) {
        selectedAnswer = answer
        resetOptionStyles()

        val selectedColor = ContextCompat.getColor(this, R.color.selected_button_background)

        when (answer) {
            btnOption1.text -> btnOption1.setBackgroundColor(selectedColor)
            btnOption2.text -> btnOption2.setBackgroundColor(selectedColor)
            btnOption3.text -> btnOption3.setBackgroundColor(selectedColor)
        }
    }

    private fun nextQuestion() {


        // Update score
        if (selectedAnswer == questionList[currentQuestionIndex].correctAnswer) {
            score++
        }

        // Move to next or end
        if (currentQuestionIndex < questionList.size - 1) {
            currentQuestionIndex++
            progressBar.progress = currentQuestionIndex + 1
            showQuestion()
        } else {
            endQuiz()
        }
    }


    // End the quiz
    private fun endQuiz() {
        timer?.cancel()


        val resultText = StringBuilder()
        resultText.append("Quiz Over! Your score: $score/${questionList.size}\n\n")



        txtQuestion.text = resultText.toString()

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



