package com.example.prog_poe_2025

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>> get() = _incomes

    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> get() = _expenses

    /**
     * Loads all Income documents from:
     *   users/{userId}/incomes
     * and posts them (newest‐first) to _incomes.
     */
    fun loadUserIncomes(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("incomes")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val incomeList = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    val categoryId = doc.getString("categoryId") // May be null if not set

                    Income(
                        id = id,
                        amount = amount,
                        date = date,
                        categoryId = categoryId
                    )
                }

                _incomes.postValue(incomeList)
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading incomes", e)
                _incomes.postValue(emptyList())
            }
        }
    }

    /**
     * Loads all Expense documents from:
     *   users/{userId}/expenses
     * and posts them (newest‐first) to _expenses.
     */
    fun loadUserExpenses(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("expenses")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val expenseList = snapshot.documents.mapNotNull { doc ->
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

                _expenses.postValue(expenseList)
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading expenses", e)
                _expenses.postValue(emptyList())
            }
        }
    }

    /**
     * Saves a new Income under:
     *   users/{userId}/incomes
     * Then reloads the list.
     */
    fun saveIncome(userId: String, income: Income) {
        viewModelScope.launch {
            try {
                val transactionData = mapOf(
                    "amount" to income.amount,
                    "date" to income.date,
                    "categoryId" to income.categoryId
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("incomes")
                    .add(transactionData)
                    .await()

                // Refresh the LiveData list
                loadUserIncomes(userId)
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error saving income", e)
            }
        }
    }

    /**
     * Saves a new Expense under:
     *   users/{userId}/expenses
     * Then reloads the list.
     */
    fun saveExpense(userId: String, expense: Expense) {
        viewModelScope.launch {
            try {
                val transactionData = mapOf(
                    "amount" to expense.amount,
                    "date" to expense.date,
                    "categoryId" to expense.categoryId
                )

                firestore.collection("users")
                    .document(userId)
                    .collection("expenses")
                    .add(transactionData)
                    .await()

                // Refresh the LiveData list
                loadUserExpenses(userId)
            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error saving expense", e)
            }
        }
    }
}

//(Medium,2023)

/*References List
Svaghasiya, 2023. Using ViewModel in Android With Kotlin, 18 September 2023. [Online]. Available at:
https://medium.com/@ssvaghasiya61/using-viewmodel-in-android-with-kotlin-16ca735c644f [Accessed 25 April 2025].
*
* */



