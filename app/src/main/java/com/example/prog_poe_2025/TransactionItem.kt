package com.example.prog_poe_2025

import Data_Classes.Expenses
import Data_Classes.Income

sealed class TransactionItem {
    abstract val date: Long

    data class IncomeItem(val income: Income): TransactionItem(){
        override val date: Long
            get() = income.date
    }
    data class ExpenseItem(val expense: Expenses): TransactionItem(){
        override val date: Long
            get() = expense.date
    }


}