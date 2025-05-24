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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Local data class representing a quiz question.
data class QuizQuestion(
    val text: String = "",
    val option1: String = "",
    val option2: String = "",
    val option3: String = "",
    val correctAnswer: String = ""
)

class BudgetQuiz : AppCompatActivity() {

    // UI components
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

    // Quiz state
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswer: String? = null
    private var timer: CountDownTimer? = null

    // Firebase instance for quiz questions
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var quizQuestions: List<QuizQuestion> = listOf()

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

        // Setup quiz control listeners
        btnStartQuiz.setOnClickListener {
            loadQuizQuestions()  // Load quiz questions from Firestore
            startSpinningTitle()
            startBackgroundColorAnimation()
            startQuiz()
        }
        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }
    }

    // Load quiz questions from the "quizQuestions" collection in Firestore.
    private fun loadQuizQuestions() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("quizQuestions").get().await()
                quizQuestions = snapshot.documents.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    val option1 = doc.getString("option1") ?: return@mapNotNull null
                    val option2 = doc.getString("option2") ?: return@mapNotNull null
                    val option3 = doc.getString("option3") ?: return@mapNotNull null
                    val correctAnswer = doc.getString("correctAnswer") ?: return@mapNotNull null
                    QuizQuestion(text, option1, option2, option3, correctAnswer)
                }
                withContext(Dispatchers.Main) {
                    if (quizQuestions.isNotEmpty()) {
                        progressBar.max = quizQuestions.size
                    } else {
                        txtQuestion.text = "No quiz questions available."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    txtQuestion.text = "Error loading quiz questions."
                }
            }
        }
    }

    // Animate the background color of the intro text.
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

    // Spin the quiz heading.
    private fun startSpinningTitle() {
        val flip = ObjectAnimator.ofFloat(txtQuizHeading, "rotationY", 0f, 360f).apply {
            duration = 2500
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }
        flip.start()
    }

    // Prepare and start the quiz.
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
        if (quizQuestions.isNotEmpty()) {
            currentQuestionIndex = 0
            score = 0
            progressBar.progress = 1
            showQuestion()
        } else {
            txtQuestion.text = "No quiz questions available."
        }
    }

    // Reset the background colors of the option buttons.
    private fun resetAnswerButtons() {
        val defaultColor = ContextCompat.getColor(this, R.color.default_button_background)
        btnOption1.setBackgroundColor(defaultColor)
        btnOption2.setBackgroundColor(defaultColor)
        btnOption3.setBackgroundColor(defaultColor)
    }

    // Display the current question and its options.
    private fun showQuestion() {
        val question = quizQuestions[currentQuestionIndex]
        resetAnswerButtons()
        txtQuestion.text = question.text
        btnOption1.text = question.option1
        btnOption2.text = question.option2
        btnOption3.text = question.option3
        selectedAnswer = null
    }

    // Reset option styles (currently the same as resetting answer buttons).
    private fun resetOptionStyles() {
        resetAnswerButtons()
    }

    // Record the selected answer and highlight its button.
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

    // Proceed to the next question or end the quiz.
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

    // End the quiz and display the final score.
    private fun endQuiz() {
        timer?.cancel()
        txtQuestion.text = "Quiz Over! Your score: $score/${quizQuestions.size}"
        btnOption1.visibility = View.GONE
        btnOption2.visibility = View.GONE
        btnOption3.visibility = View.GONE
        btnNext.visibility = View.GONE
        txtTimer.visibility = View.GONE
    }

    // Start an emoji-based countdown timer.
    private fun startEmojiCountdown() {
        val emojis = listOf("⏳", "😬", "😱", "💣", "🔥")
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

