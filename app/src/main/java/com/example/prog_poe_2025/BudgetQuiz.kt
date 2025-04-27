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
    private var quizQuestions: List<Questions> = listOf()

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
        btnStartQuiz.setOnClickListener { preloadQuestionsAndStartQuiz() }
        btnOption1.setOnClickListener { selectAnswer(btnOption1.text.toString()) }
        btnOption2.setOnClickListener { selectAnswer(btnOption2.text.toString()) }
        btnOption3.setOnClickListener { selectAnswer(btnOption3.text.toString()) }
        btnNext.setOnClickListener { nextQuestion() }
    }

    private fun preloadQuestionsAndStartQuiz() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Preload questions if database is empty
                if (database.questionsDao().getAllQuestions().isEmpty()) {
                    val defaultQuestions = listOf(
                        Questions(
                            text = "What is budgeting?",
                            option1 = "A plan for money",
                            option2 = "A shopping list",
                            option3 = "A bank account",
                            correctAnswer = "A plan for money"
                        ),
                        Questions(
                            text = "Which expense is fixed?",
                            option1 = "Rent",
                            option2 = "Groceries",
                            option3 = "Entertainment",
                            correctAnswer = "Rent"
                        ),
                        Questions(
                            text = "What is an emergency fund?",
                            option1 = "Money saved for surprises",
                            option2 = "A travel fund",
                            option3 = "A loan",
                            correctAnswer = "Money saved for surprises"
                        ),
                        Questions(
                            text = "Which of these is a variable expense?",
                            option1 = "Electricity bill",
                            option2 = "Internet subscription",
                            option3 = "Dining out",
                            correctAnswer = "Dining out"
                        ),
                        Questions(
                            text = "What does income mean?",
                            option1 = "Money you owe",
                            option2 = "Money you save",
                            option3 = "Money you earn",
                            correctAnswer = "Money you earn"
                        ),
                        Questions(
                            text = "What is the 50/30/20 rule?",
                            option1 = "Spending rule",
                            option2 = "Tax rule",
                            option3 = "Cooking rule",
                            correctAnswer = "Spending rule"
                        ),
                        Questions(
                            text = "Why should you track expenses?",
                            option1 = "To get more money",
                            option2 = "To win a prize",
                            option3 = "To control spending",
                            correctAnswer = "To control spending"
                        ),
                        Questions(
                            text = "Which is a financial goal?",
                            option1 = "Buying snacks",
                            option2 = "Buying a car",
                            option3 = "Watching TV",
                            correctAnswer = "Buying a car"
                        ),
                        Questions(
                            text = "How often should you review your budget?",
                            option1 = "Once a year",
                            option2 = "Never",
                            option3 = "Every month",
                            correctAnswer = "Every month"
                        ),
                        Questions(
                            text = "What is net income?",
                            option1 = "Income before tax",
                            option2 = "Income after tax",
                            option3 = "Gift money",
                            correctAnswer = "Income after tax"
                        ),

                        // Scenario-based questions
                        Questions(
                            text = "You received a bonus of $100. What should you do first?",
                            option1 = "Spend it all on clothes",
                            option2 = "Put it in savings",
                            option3 = "Ignore it",
                            correctAnswer = "Put it in savings"
                        ),
                        Questions(
                            text = "Your rent just increased. What should you adjust in your budget?",
                            option1 = "Cut other expenses",
                            option2 = "Spend more",
                            option3 = "Do nothing",
                            correctAnswer = "Cut other expenses"
                        ),
                        Questions(
                            text = "You forgot to budget for your friend's birthday gift. What is this called?",
                            option1 = "A fixed expense",
                            option2 = "A surprise cost",
                            option3 = "A planned cost",
                            correctAnswer = "A surprise cost"
                        ),
                        Questions(
                            text = "You overspent on entertainment this month. What should you do next?",
                            option1 = "Ignore it",
                            option2 = "Borrow money",
                            option3 = "Adjust next month’s budget",
                            correctAnswer = "Adjust next month’s budget"
                        ),
                        Questions(
                            text = "If your income decreases, what should you do?",
                            option1 = "Spend more",
                            option2 = "Adjust your budget",
                            option3 = "Ignore the change",
                            correctAnswer = "Adjust your budget"
                        ),
                        Questions(
                            text = "You want to save for a laptop in 3 months. What's your best budgeting strategy?",
                            option1 = "Cut all spending",
                            option2 = "Set a monthly saving target",
                            option3 = "Buy it now on credit",
                            correctAnswer = "Set a monthly saving target"
                        ),
                        Questions(
                            text = "Your grocery bill is higher this month. What might be a good action?",
                            option1 = "Eat out more",
                            option2 = "Shop with a list",
                            option3 = "Ignore receipts",
                            correctAnswer = "Shop with a list"
                        ),
                        Questions(
                            text = "You're offered two part-time jobs. One pays more but has no benefits. What should you consider?",
                            option1 = "Only the salary",
                            option2 = "Long-term benefits",
                            option3 = "The hours alone",
                            correctAnswer = "Long-term benefits"
                        ),
                        Questions(
                            text = "You’re tempted by a flash sale. What should you ask yourself?",
                            option1 = "Is it trendy?",
                            option2 = "Do I really need this?",
                            option3 = "Can I show it off?",
                            correctAnswer = "Do I really need this?"
                        ),
                        Questions(
                            text = "You set a budget goal but missed it. What should you do?",
                            option1 = "Give up",
                            option2 = "Review and try again",
                            option3 = "Ignore the budget",
                            correctAnswer = "Review and try again"
                        ),

                        // More budgeting basics
                        Questions(
                            text = "Which of these is the best reason to have a budget?",
                            option1 = "To impress others",
                            option2 = "To manage money better",
                            option3 = "To get rich quick",
                            correctAnswer = "To manage money better"
                        ),
                        Questions(
                            text = "What is a discretionary expense?",
                            option1 = "Essential to survival",
                            option2 = "Optional spending",
                            option3 = "Government fees",
                            correctAnswer = "Optional spending"
                        ),
                        Questions(
                            text = "If you consistently overspend, what might help?",
                            option1 = "Making a budget",
                            option2 = "Spending faster",
                            option3 = "Avoiding bank accounts",
                            correctAnswer = "Making a budget"
                        ),
                        Questions(
                            text = "What’s the first step in budgeting?",
                            option1 = "Spend first",
                            option2 = "Track income and expenses",
                            option3 = "Guess amounts",
                            correctAnswer = "Track income and expenses"
                        ),
                        Questions(
                            text = "Why save for retirement early?",
                            option1 = "For taxes",
                            option2 = "For future security",
                            option3 = "To show off wealth",
                            correctAnswer = "For future security"
                        ),
                        Questions(
                            text = "Which of these is a need, not a want?",
                            option1 = "Streaming service",
                            option2 = "Groceries",
                            option3 = "New shoes",
                            correctAnswer = "Groceries"
                        ),
                        Questions(
                            text = "What is compound interest?",
                            option1 = "Interest on interest",
                            option2 = "A monthly fee",
                            option3 = "A loan charge",
                            correctAnswer = "Interest on interest"
                        )
                    )

                    database.questionsDao().insertAll(defaultQuestions)
                }
            }

            // Fetch 10 random questions
            quizQuestions = database.questionsDao().getAllQuestions()
            startBackgroundColorAnimation()
            startQuiz()
        }
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