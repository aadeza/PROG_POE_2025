package com.example.prog_poe_2025

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.MotionEvent
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

    private var holdStartTime: Long = 0
    private val holdDuration = 2000L // 2 seconds

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
        txtQuizHeading = findViewById(R.id.txtQuizHeading)  // Initialize title reference

        // Start quiz when button is clicked
        btnStartQuiz.setOnClickListener {
            startQuiz()
        }

        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }

        // Start the spinning animation every 4 seconds
        startSpinningTitle()

        // Start the background color transition
        startBackgroundColorAnimation()

        // Set the touch listener for the Start Quiz button
        btnStartQuiz.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start holding the button, start the color transition
                    startButtonHold()
                    holdStartTime = System.currentTimeMillis() // Store the time when holding starts
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Check if the hold duration was met and start the quiz
                    val holdEndTime = System.currentTimeMillis()
                    if (holdEndTime - holdStartTime >= holdDuration) {
                        startQuiz() // Start the quiz if the button was held long enough
                    } else {
                        resetButtonState() // Reset button if the duration was too short
                    }
                }
            }
            true
        }
    }

    // Function to animate the background color transition for txtIntro
    private fun startBackgroundColorAnimation() {
        // Define colors (blue to dark purple)
        val colorFrom = Color.parseColor("#00008b")
        val colorTo = Color.parseColor("#800080")

        // Set up the animator to transition between the colors
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 5000 // Duration of the animation in milliseconds (5 seconds)
        colorAnimation.repeatMode = ValueAnimator.REVERSE // Reverse the animation back to the starting color
        colorAnimation.repeatCount = ValueAnimator.INFINITE // Keep the animation repeating infinitely

        // Update the background color during the animation
        colorAnimation.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            txtIntro.setBackgroundColor(animatedValue)
        }
        // Start the animation
        colorAnimation.start()
    }

    // Function to make the title spin on the Y-axis
    private fun startSpinningTitle() {
        val handler = Handler()

        val flipRunnable = object : Runnable {
            override fun run() {
                // Perform the rotation on the Y-axis (like turning its back)
                val flip = ObjectAnimator.ofFloat(txtQuizHeading, "rotationY", 0f, 360f)
                flip.duration = 1000  // Duration of the flip (can adjust)
                flip.start()

                // Post this runnable again after 4 seconds (4000 milliseconds)
                handler.postDelayed(this, 4000)
            }
        }

        // Start the first rotation
        handler.post(flipRunnable)
    }

    // Function to start holding the button with color transition
    private fun startButtonHold() {
        val backgroundColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#6200EE"), Color.WHITE)
        backgroundColorAnimator.duration = holdDuration // Animate for 2 seconds
        backgroundColorAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            btnStartQuiz.setBackgroundColor(animatedValue)
        }

        val textColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.WHITE, Color.parseColor("#0000FF")) // White to blue
        textColorAnimator.duration = holdDuration
        textColorAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            btnStartQuiz.setTextColor(animatedValue)
        }

        // Start both animations simultaneously
        backgroundColorAnimator.start()
        textColorAnimator.start()
    }

    // Function to reset the button state after the hold action is released
    private fun resetButtonState() {
        // Reset to original colors
        val backgroundColorReset = ValueAnimator.ofObject(ArgbEvaluator(), Color.WHITE, Color.parseColor("#6200EE"))
        backgroundColorReset.duration = 300 // Short duration to reset
        backgroundColorReset.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            btnStartQuiz.setBackgroundColor(animatedValue)
        }

        val textColorReset = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#0000FF"), Color.WHITE)
        textColorReset.duration = 300
        textColorReset.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            btnStartQuiz.setTextColor(animatedValue)
        }

        // Start both reset animations
        backgroundColorReset.start()
        textColorReset.start()
    }

    // Function to start the quiz
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

    // Function to start the timer
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

    // Function to show the current question
    private fun showQuestion() {
        val question = quizQuestions[currentQuestionIndex]
        txtQuestion.text = question.text
        btnOption1.text = question.option1
        btnOption2.text = question.option2
        btnOption3.text = question.option3
        selectedAnswer = null
    }

    // Function to handle answer selection
    private fun selectAnswer(answer: String) {
        selectedAnswer = answer
    }

    // Function to move to the next question
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

    // Function to end the quiz and display the score
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

// Data class for the questions in the quiz
data class Question(
    val text: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correctAnswer: String
)
