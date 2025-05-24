package com.example.prog_poe_2025

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertIncome(userId: String, income: Income) {
        firestore.collection("users").document(userId)
            .collection("incomes").add(income).await()
    }

    suspend fun insertExpense(userId: String, expense: Expense) {
        firestore.collection("users").document(userId)
            .collection("expenses").add(expense).await()
    }

    suspend fun getUserIncome(userId: String): List<Income> {
        return try {
            val snapshot: QuerySnapshot = firestore.collection("users").document(userId)
                .collection("incomes").get().await()

            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val amount = doc.getDouble("amount") ?: return@mapNotNull null
                val date = doc.getLong("date") ?: return@mapNotNull null
                Income(id, amount, date)
            }
        } catch (e: Exception) {
            emptyList() // Return an empty list on failure
        }
    }

    suspend fun getUserExpense(userId: String): List<Expense> {
        return try {
            val snapshot: QuerySnapshot = firestore.collection("users").document(userId)
                .collection("expenses").get().await()

            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val amount = doc.getDouble("amount") ?: return@mapNotNull null
                val date = doc.getLong("date") ?: return@mapNotNull null
                Expense(id, amount, date)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// (W3Schools,2025)

/*
Reference List:
W3Schools. 2025. Kotlin Tutorial, n.d.[Online]. Available at:
https://www.w3schools.com/kotlin/index.php  [Accessed 24 April 2025].
 */