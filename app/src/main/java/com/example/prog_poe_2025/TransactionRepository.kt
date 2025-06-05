package com.example.prog_poe_2025

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Inserts a new Income for the given user.
     * The Firestore path is: users/{userId}/incomes/{auto‐generated‐doc}
     *
     * @param userId       The UID of the current user.
     * @param income       An Income instance (id field is ignored for insertion).
     */
    suspend fun insertIncome(userId: String, income: Income) {
        // Build a map of only the fields we want to write:
        val data = hashMapOf(
            "amount" to income.amount,
            "date" to income.date,
            "categoryId" to income.categoryId
        )

        firestore.collection("users")
            .document(userId)
            .collection("incomes")
            .add(data)
            .await()
    }

    /**
     * Inserts a new Expense for the given user.
     * The Firestore path is: users/{userId}/expenses/{auto‐generated‐doc}
     *
     * @param userId       The UID of the current user.
     * @param expense      An Expense instance (id field is ignored for insertion).
     */
    suspend fun insertExpense(userId: String, expense: Expense) {
        val data = hashMapOf(
            "amount" to expense.amount,
            "date" to expense.date,
            "categoryId" to expense.categoryId
        )

        firestore.collection("users")
            .document(userId)
            .collection("expenses")
            .add(data)
            .await()
    }

    /**
     * Fetches all Income documents under users/{userId}/incomes,
     * and returns them as a List<Income>, preserving categoryId if present.
     */
    suspend fun getUserIncome(userId: String): List<Income> {
        return try {
            val snapshot: QuerySnapshot = firestore.collection("users")
                .document(userId)
                .collection("incomes")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val amount = doc.getDouble("amount") ?: return@mapNotNull null
                val date = doc.getLong("date") ?: return@mapNotNull null
                // If categoryId field is missing or null, we pass null
                val categoryId = doc.getString("categoryId")

                Income(
                    id = id,
                    amount = amount,
                    date = date,
                    categoryId = categoryId
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches all Expense documents under users/{userId}/expenses,
     * and returns them as a List<Expense>, preserving categoryId if present.
     */
    suspend fun getUserExpense(userId: String): List<Expense> {
        return try {
            val snapshot: QuerySnapshot = firestore.collection("users")
                .document(userId)
                .collection("expenses")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val id = doc.id
                val amount = doc.getDouble("amount") ?: return@mapNotNull null
                val date = doc.getLong("date") ?: return@mapNotNull null
                val categoryId = doc.getString("categoryId")

                Expense(
                    id = id,
                    amount = amount,
                    date = date,
                    categoryId = categoryId
                )
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