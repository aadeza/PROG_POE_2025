package com.example.prog_poe_2025

import DAOs.ExpensesDAO
import DAOs.IncomeDAO
import Data_Classes.Expenses
import Data_Classes.Income
import com.example.prog_poe_2025.CurrencyConverter
import com.example.prog_poe_2025.TransactionItem
import kotlinx.coroutines.flow.first
import kotlin.math.roundToLong

class HomeRepository(
    private val incomeDao: IncomeDAO,
    private val expensesDao: ExpensesDAO
) {


    suspend fun getLatestTransactions(preferredCurrency: String): List<TransactionItem> {
        val latestIncomes = incomeDao.getLatestIncomes(5).map { income ->
            TransactionItem.IncomeItem(
                income = income.copy(
                    amount = CurrencyConverter.convertAmount(
                        income.amount,
                        "ZAR",
                        preferredCurrency
                    ).toLong()
                )
            )
        }

        val latestExpenses = expensesDao.getLatestExpenses(5).map { expense ->
            TransactionItem.ExpenseItem(
                expense = expense.copy(
                    amount = CurrencyConverter.convertAmount(
                        expense.amount,
                        "ZAR",
                        preferredCurrency
                    ).toLong()
                )
            )
        }

        // Combine, sort by date (descending), and return top 5
        return (latestIncomes + latestExpenses)
            .sortedByDescending { it.date }
            .take(5)
    }

    // Fetch and convert latest incomes
    suspend fun getConvertedLatestIncomes(limit: Int, preferredCurrency: String): List<Income> {
        val incomes = incomeDao.getLatestIncomes(limit)
        return incomes.map { income ->
            try {
                val convertedAmount =
                    CurrencyConverter.convertAmount(income.amount, "ZAR", preferredCurrency)
                income.copy(amount = convertedAmount.toLong())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                income
            }
        }
    }
}
