package com.example.prog_poe_2025

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class TransactionHistory : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val database = AppDatabase.getDatabase(applicationContext)
        val incomeDao = database.incomeDao()
        val expenseDao = database.expensesDao()
        val repository = HomeRepository(incomeDao, expenseDao)
        val factory = HomeViewModelFactory(application, repository)

        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        viewModel.allTransactions.observe(this) { transactions ->
            adapter = TransactionAdapter(transactions)
            recyclerView.adapter = adapter
        }

        viewModel.fetchAllTransactions("USD") // Pass the preferred currency dynamically
    }
}
