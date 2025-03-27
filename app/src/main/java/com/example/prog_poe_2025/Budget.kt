package com.example.prog_poe_2025
import java.time.LocalDate

data class Budget(
    val budgetName: String,
    val budgetDescription: String,
    val totalAmount: Double,
    val currentAmount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val categories: List<String>,
    val progress: Int,
    val lastUpdated: LocalDate
)

data class Transaction(
    val amount: Double,
    val date: LocalDate,
    val description: String
)


