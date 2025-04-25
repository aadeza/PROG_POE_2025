package com.example.prog_poe_2025

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Questions::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun questionsDao(): QuestionsDAO

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope) : AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance

            }
        }
    }

    private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.questionsDao())
                }

            }
        }

        suspend fun populateDatabase(dao: QuestionsDAO){
            val questionList = listOf(
                Questions(text = "What is budgeting?", option1 = "A plan for money", option2 = "A shopping list", option3 = "A bank account", correctAnswer = "A plan for money"),
                Questions(text = "Which expense is fixed?", option1 = "Rent", option2 = "Groceries", option3 = "Entertainment", correctAnswer = "Rent"),
                Questions(text = "What is an emergency fund?", option1 = "Money saved for surprises", option2 = "A travel fund", option3 = "A loan", correctAnswer = "Money saved for surprises"),
                Questions(text = "Which of these is a variable expense?", option1 = "Electricity bill", option2 = "Internet subscription", option3 = "Dining out", correctAnswer = "Dining out"),
                Questions(text = "What does income mean?", option1 = "Money you owe", option2 = "Money you save", option3 = "Money you earn", correctAnswer = "Money you earn"),
                Questions(text = "What is the 50/30/20 rule?", option1 = "Spending rule", option2 = "Tax rule", option3 = "Cooking rule", correctAnswer = "Spending rule"),
                Questions(text = "Why should you track expenses?", option1 = "To get more money", option2 = "To win a prize", option3 = "To control spending", correctAnswer = "To control spending"),
                Questions(text = "Which is a financial goal?", option1 = "Buying snacks", option2 = "Buying a car", option3 = "Watching TV", correctAnswer = "Buying a car"),
                Questions(text = "How often should you review your budget?", option1 = "Once a year", option2 = "Never", option3 = "Every month", correctAnswer = "Every month"),
                Questions(text = "What is net income?", option1 = "Income before tax", option2 = "Income after tax", option3 = "Gift money", correctAnswer = "Income after tax"),

                // Scenario-based questions
                Questions(text = "You received a bonus of $100. What should you do first?", option1 = "Spend it all on clothes", option2 = "Put it in savings", option3 = "Ignore it", correctAnswer = "Put it in savings"),
                Questions(text = "Your rent just increased. What should you adjust in your budget?", option1 = "Cut other expenses", option2 = "Spend more", option3 = "Do nothing", correctAnswer = "Cut other expenses"),
                Questions(text = "You forgot to budget for your friend's birthday gift. What is this called?", option1 = "A fixed expense", option2 = "A surprise cost", option3 = "A planned cost", correctAnswer = "A surprise cost"),
                Questions(text = "You overspent on entertainment this month. What should you do next?", option1 = "Ignore it", option2 = "Borrow money", option3 = "Adjust next month’s budget", correctAnswer = "Adjust next month’s budget"),
                Questions(text = "If your income decreases, what should you do?", option1 = "Spend more", option2 = "Adjust your budget", option3 = "Ignore the change", correctAnswer = "Adjust your budget"),
                Questions(text = "You want to save for a laptop in 3 months. What's your best budgeting strategy?", option1 = "Cut all spending", option2 = "Set a monthly saving target", option3 = "Buy it now on credit", correctAnswer = "Set a monthly saving target"),
                Questions(text = "Your grocery bill is higher this month. What might be a good action?", option1 = "Eat out more", option2 = "Shop with a list", option3 = "Ignore receipts", correctAnswer = "Shop with a list"),
                Questions(text = "You're offered two part-time jobs. One pays more but has no benefits. What should you consider?", option1 = "Only the salary", option2 = "Long-term benefits", option3 = "The hours alone", correctAnswer = "Long-term benefits"),
                Questions(text = "You’re tempted by a flash sale. What should you ask yourself?", option1 = "Is it trendy?", option2 = "Do I really need this?", option3 = "Can I show it off?", correctAnswer = "Do I really need this?"),
                Questions(text = "You set a budget goal but missed it. What should you do?", option1 = "Give up", option2 = "Review and try again", option3 = "Ignore the budget", correctAnswer = "Review and try again"),

                // More budgeting basics
                Questions(text = "Which of these is the best reason to have a budget?", option1 = "To impress others", option2 = "To manage money better", option3 = "To get rich quick", correctAnswer = "To manage money better"),
                Questions(text = "What is a discretionary expense?", option1 = "Essential to survival", option2 = "Optional spending", option3 = "Government fees", correctAnswer = "Optional spending"),
                Questions(text = "If you consistently overspend, what might help?", option1 = "Making a budget", option2 = "Spending faster", option3 = "Avoiding bank accounts", correctAnswer = "Making a budget"),
                Questions(text = "What’s the first step in budgeting?", option1 = "Spend first", option2 = "Track income and expenses", option3 = "Guess amounts", correctAnswer = "Track income and expenses"),
                Questions(text = "Why save for retirement early?", option1 = "For taxes", option2 = "For future security", option3 = "To show off wealth", correctAnswer = "For future security"),
                Questions(text = "Which of these is a need, not a want?", option1 = "Streaming service", option2 = "Groceries", option3 = "New shoes", correctAnswer = "Groceries"),
                Questions(text = "What is compound interest?", option1 = "Interest on interest", option2 = "A monthly fee", option3 = "A loan charge", correctAnswer = "Interest on interest")
            )

            dao.insertAll(questionList)

        }
    }
}




