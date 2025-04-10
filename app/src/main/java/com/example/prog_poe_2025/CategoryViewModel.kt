package com.example.prog_poe_2025

import Data_Classes.Category
import DAOs.CategoryDAO
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()

    val categories: LiveData<List<Category>> = categoryDao.getAllCategories()

    fun insert(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }

    fun insertAll(categories: List<Category>) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insert(categories)
        }
    }
}
