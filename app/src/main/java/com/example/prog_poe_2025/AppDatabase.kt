package com.example.prog_poe_2025

import DAOs.*
import Data_Classes.*
import android.content.Context
import androidx.room.*

@Database(
    entities = [
        Users::class,
        Budgets::class,
        Category::class,
        Expenses::class,
        Income::class,
        BudgetCategoryCrossRef::class,
        QuizScores::class
    ],
    version = 11, // ✅ Ensure the version number matches changes in schema
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
                    .fallbackToDestructiveMigration() // ✅ Wipes all data when schema changes
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}