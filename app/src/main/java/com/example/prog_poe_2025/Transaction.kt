package com.example.prog_poe_2025

open class spTransaction(
    open val amount: Long,
    open val description: String?,
    open val category: String,
    open val date: Long, // ✅ Shared across both Income & Expenses
    open val transaction_type: String,
    open val imagePath: String?,
    open val user_id: Int,
    open val budgetId: Int,
    open val isExpense: Boolean
)