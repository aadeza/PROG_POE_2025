package com.example.prog_poe_2025

import Data_Classes.Expenses
import Data_Classes.Income
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.sql.Date

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    val incomes = MutableLiveData<List<Income>>()
    val expenses = MutableLiveData<List<Expenses>>()





    fun loadUserIncomes(userId : Int){
        viewModelScope.launch{
            incomes.value = repository.getUserIncome(userId)
        }
    }

    fun loadUserExpenses(userId: Int){
        viewModelScope.launch{
            expenses.value = repository.getUserExpense(userId)
        }
    }

    fun saveIncome(income : Income){
        viewModelScope.launch{
            repository.insertIncome(income)
        }
    }

    fun saveExpense(expense : Expenses){
        viewModelScope.launch{
            repository.insertExpenses(expense)
        }
    }



}//(Medium,2023)

/*References List
Svaghasiya, 2023. Using ViewModel in Android With Kotlin, 18 September 2023. [Online]. Available at:
https://medium.com/@ssvaghasiya61/using-viewmodel-in-android-with-kotlin-16ca735c644f [Accessed 25 April 2025].
*
* */



