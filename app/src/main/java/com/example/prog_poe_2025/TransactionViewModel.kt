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

    fun loadUserIncomes(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("incomes").orderBy("date").get().await()

                val incomeList = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    Income(id, amount, date)
                }

                _incomes.postValue(incomeList)

            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading incomes", e)
            }
        }
    }

    fun loadUserExpenses(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("expenses").orderBy("date").get().await()

                val expenseList = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val amount = doc.getDouble("amount") ?: return@mapNotNull null
                    val date = doc.getLong("date") ?: return@mapNotNull null
                    Expense(id, amount, date)
                }

                _expenses.postValue(expenseList)

            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error loading expenses", e)
            }
        }
    }

    fun saveIncome(userId: String, income: Income) {
        viewModelScope.launch {
            try {
                val transactionData = mapOf(
                    "amount" to income.amount,
                    "date" to income.date,
                    "isExpense" to income.isExpense
                )

                firestore.collection("users").document(userId)
                    .collection("incomes").add(transactionData).await()

                loadUserIncomes(userId) // Refresh list

            } catch (e: Exception) {
                Log.e("TransactionViewModel", "Error saving income", e)
            }
        }
    }

    fun saveExpense(userId: String, expense: Expense) {
        viewModelScope.launch {
            try {
                val transactionData = mapOf(
                    "amount" to expense.amount,
                    "date" to expense.date,
                    "isExpense" to expense.isExpense
                )

                firestore.collection("users").document(userId)
                    .collection("expenses").add(transactionData).await()

                loadUserExpenses(userId) // Refresh list

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



