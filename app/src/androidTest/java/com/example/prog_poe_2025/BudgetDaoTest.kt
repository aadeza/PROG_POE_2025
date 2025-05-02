package com.example.prog_poe_2025

import DAOs.BudgetDAO
import Data_Classes.Budgets
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var budgetDao: BudgetDAO

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        budgetDao = db.budgetDao()
    }

    @Test
    fun testBudgetInsertion() = runBlocking {
        val budget = Budgets(
            name = "Test Budget",
            maxMonthGoal = 5000L,
            user_id = 1,
            budgetType = "Personal",
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000),
            minMonthGoal = 1000L
        )

        val budgetId = budgetDao.insertBudget(budget)
        val retrievedBudget = budgetDao.getBudgetById(budgetId.toInt())

        assertNotNull(retrievedBudget) // ✅ Budget should exist
        assertEquals("Test Budget", retrievedBudget?.name)
    }

    @After
    fun cleanup() {
        db.close()
    }
}