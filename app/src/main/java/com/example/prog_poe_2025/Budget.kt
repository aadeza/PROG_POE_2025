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

// (W3Schools,2025)

/*
Reference List:
W3Schools. 2025. Kotlin Tutorial, n.d.[Online]. Available at:
https://www.w3schools.com/kotlin/index.php  [Accessed 24 April 2025].
 */

