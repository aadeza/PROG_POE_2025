package com.example.prog_poe_2025

import DAOs.ExpensesDAO
import DAOs.IncomeDAO
import Data_Classes.Expenses
import Data_Classes.Income


class HomeRepository(
    private val incomeDao: IncomeDAO,
    private val expensesDao: ExpensesDAO
) {

    suspend fun getLatestIncomes(limit: Int): List<Income> {
        return incomeDao.getLatestIncomes(limit)
    }

    suspend fun getLatestExpenses(limit: Int): List<Expenses> {
        return expensesDao.getLatestExpenses(limit)
    }


}
