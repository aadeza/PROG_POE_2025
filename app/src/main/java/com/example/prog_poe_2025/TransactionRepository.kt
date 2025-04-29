package com.example.prog_poe_2025

import DAOs.ExpensesDAO
import DAOs.IncomeDAO
import Data_Classes.Expenses
import Data_Classes.Income
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val incomeDAO: IncomeDAO, private val expensesDAO:  ExpensesDAO) {
    suspend fun insertIncome(income: Income) = incomeDAO.insertIncome(income)
    suspend fun insertExpenses(expenses: Expenses) = expensesDAO.insertExpense(expenses)
    suspend fun getUserIncome(userId: Int) = incomeDAO.getIncomeByUser(userId)
    suspend fun getUserExpense(userId: Int) = expensesDAO.getExpensesByUser(userId)
    fun getTotalIncome(userId: Int): Flow<Long?> = incomeDAO.getTotalIncome(userId)
    fun getTotalExpenses(userId: Int): Flow<Long?> = expensesDAO.getTotalExpenses(userId)


}