package com.example.prog_poe_2025



sealed class TransactionItem {
    abstract val date: Long

    data class IncomeItem(val income: Income) : TransactionItem() {
        override val date: Long
            get() = income.date
    }

    data class ExpenseItem(val expense: Expense) : TransactionItem() {
        override val date: Long
            get() = expense.date
    }
}
