package com.example.prog_poe_2025

import DAOs.BudgetDAO
import Data_Classes.Budgets
import Data_Classes.BudgetCategoryCrossRef
import Data_Classes.Category
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val budgetDao = AppDatabase.getDatabase(application).budgetDao()
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()

    // We want LiveData for observing in the UI
    val categories: LiveData<List<Category>> = categoryDao.getAllCategoriesLive()

    // Insert a single category
    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }

    // Insert multiple categories
    fun insertAll(categories: List<Category>) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insertAll(categories)
        }
    }

    // Delete all categories if needed
    fun deleteAllCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.deleteAllCategories()
        }
    }

    // Insert a new budget
    suspend fun insertBudget(budget: Budgets): Long {
        return budgetDao.insertBudget(budget)
    }


    // Insert BudgetCategoryCrossRef to link categories to a budget
    suspend fun insertBudgetCategoryCrossRefs(crossRefs: List<BudgetCategoryCrossRef>) {
        budgetDao.insertBudgetCategoryCrossRefs(crossRefs)
    }
}

