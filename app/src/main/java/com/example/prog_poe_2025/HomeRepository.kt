package com.example.prog_poe_2025

import DAOs.ExpensesDAO
import DAOs.IncomeDAO
import Data_Classes.Expenses
import Data_Classes.Income
import com.example.prog_poe_2025.CurrencyConverter
import com.example.prog_poe_2025.TransactionItem
import kotlin.math.roundToLong

class HomeRepository(
    private val incomeDao: IncomeDAO,
    private val expensesDao: ExpensesDAO
) {

    /**
     * Gets the total income for a specific user.
     * @param userId The user's ID.
     * @return Total income as a Long or null if no data is found.
     */
    fun getTotalIncome(userId: Int): Long? {
        return incomeDao.getTotalIncome(userId)
    }

    /**
     * Gets the total expenses for a specific user.
     * @param userId The user's ID.
     * @return Total expenses as a Long or null if no data is found.
     */
    fun getTotalExpenses(userId: Int): Long? {
        return expensesDao.getTotalExpenses(userId)
    }

    /**
     * Fetches the latest transactions (income and expenses), converts them to the user's preferred currency,
     * and sorts them by date in descending order.
     * @param preferredCurrency The user's preferred currency (e.g., USD, EUR).
     * @return A sorted list of converted transactions.
     */
    suspend fun getLatestTransactions(preferredCurrency: String): List<TransactionItem> {
        val latestIncomes = incomeDao.getLatestIncomes(5).map { income ->
            TransactionItem.IncomeItem(
                income = income.copy(
                    amount = CurrencyConverter.convertAmount(
                        income.amount, "ZAR", preferredCurrency
                    ).toLong()
                )
            )
        }

        val latestExpenses = expensesDao.getLatestExpenses(5).map { expense ->
            TransactionItem.ExpenseItem(
                expense = expense.copy(
                    amount = CurrencyConverter.convertAmount(
                        expense.amount, "ZAR", preferredCurrency
                    ).toLong()
                )
            )
        }

        // Combine incomes and expenses, sort by date in descending order
        return (latestIncomes + latestExpenses).sortedByDescending { it.date }
    }

    /**
     * Calculates net savings in the user's preferred currency.
     * @param userId The user's ID.
     * @param preferredCurrency The user's preferred currency (e.g., USD, EUR).
     * @return Net savings converted to the preferred currency.
     */
    suspend fun calculateNetSavingsInPreferredCurrency(userId: Int, preferredCurrency: String): Long {
        val totalIncome = incomeDao.getTotalIncome(userId) ?: 0L
        val totalExpenses = expensesDao.getTotalExpenses(userId) ?: 0L
        val netSavings = totalIncome - totalExpenses

        return try {
            CurrencyConverter.convertAmount(netSavings, "ZAR", preferredCurrency).roundToLong()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            netSavings // Fallback to ZAR if conversion fails
        }
    }

    /**
     * Fetches and converts a list of latest incomes to the user's preferred currency.
     * @param limit The number of latest incomes to fetch.
     * @param preferredCurrency The user's preferred currency.
     * @return A list of converted Income objects.
     */
    suspend fun getConvertedLatestIncomes(limit: Int, preferredCurrency: String): List<Income> {
        val incomes = incomeDao.getLatestIncomes(limit)
        return incomes.map { income ->
            try {
                val convertedAmount = CurrencyConverter.convertAmount(
                    income.amount, "ZAR", preferredCurrency
                )
                income.copy(amount = convertedAmount.toLong())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                income // Return original if conversion fails
            }
        }
    }

    suspend fun getAllTransactions(preferredCurrency: String): List<TransactionItem> {
        val incomes = incomeDao.getAllIncomes().map { income ->
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

        val expenses = expensesDao.getAllExpenses().map { expense ->
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
        return (incomes + expenses).sortedByDescending { it.date }
    }

        suspend fun getConvertedLatestExpenses(limit: Int, preferredCurrency: String): List<Expenses> {
        val expenses = expensesDao.getLatestExpenses(limit)
        return expenses.map { expense ->
            try {
                val convertedAmount = CurrencyConverter.convertAmount(
                    expense.amount, "ZAR", preferredCurrency
                )
                expense.copy(amount = convertedAmount.toLong())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                expense // Return original if conversion fails
            }
        }
    }
}