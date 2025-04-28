package com.example.prog_poe_2025


import DAOs.*
import Data_Classes.*
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        Users::class,
        Budgets::class,
        Category::class,
        Expenses::class,
        Income::class,
        BudgetCategoryCrossRef::class,
        QuizScores::class,
        Questions::class
    ],
    version = 5, // ⬅ Bumped version to fix schema mismatch
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDAO
    abstract fun categoryDao(): CategoryDAO
    abstract fun expensesDao(): ExpensesDAO
    abstract fun incomeDao(): IncomeDAO
    abstract fun budgetCategoryDao(): BudgetCategoryDAO
    abstract fun quizScoresDao(): QuizScoresDAO
    abstract fun questionsDao(): QuestionsDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "PennyWise"
                )
                    .fallbackToDestructiveMigration() // ✅ Auto-wipes DB if schema mismatch (use only for dev)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}




