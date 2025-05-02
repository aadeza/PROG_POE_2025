package com.example.prog_poe_2025

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private lateinit var database: AppDatabase
    private var quizQuestions: List<Data_Classes.Questions> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_quiz)

        // Initialize database and views
        database = AppDatabase.getDatabase(this)
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
        // Set up BottomNavigationView
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

                R.id.nav_game -> true // Stay here
                else -> false
            }
        }

        // Start the quiz

        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }
    }



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


    private fun startQuiz() {
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
        showQuestion()
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

    private fun resetOptionStyles() {
        val defaultColor = ContextCompat.getColor(this, R.color.default_button_background)
        btnOption1.setBackgroundColor(defaultColor)
        btnOption2.setBackgroundColor(defaultColor)
        btnOption3.setBackgroundColor(defaultColor)
    }

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
        txtQuestion.text = "Quiz Over! Your score: $score/${quizQuestions.size}"
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        btnNext.visibility = View.GONE
        txtTimer.visibility = View.GONE
    }


    private fun startEmojiCountdown() {
        val emojis = listOf("⏳", "😬", "😱", "💣", "🔥") // Different emojis for different times
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val emojiIndex = when {
                    secondsLeft > 30 -> 0 // Hourglass
                    secondsLeft > 20 -> 1 // Nervous face
                    secondsLeft > 10 -> 2 // Shocked face
                    secondsLeft > 5 -> 3 // Bomb
                    else -> 4 // Fire
                }
                txtTimer.text = "Time Left: ${secondsLeft}s ${emojis[emojiIndex]}"
            }

            override fun onFinish() {
                txtTimer.text = "Time's Up! 💥"
                endQuiz()
            }
        }.start()
    }
}
//(W3Schools,2025)

/*References List
* W3Schools, 2025. Kotlin Tutorial, n.d. [Online]. Available at:
* https://www.w3schools.com/kotlin/index.php [Accessed 19 April 2025]
* */

