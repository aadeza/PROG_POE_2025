package com.example.prog_poe_2025

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter

// Define the vbBudgets class
data class vbBudgets(
    val name: String,
    val totalBudget: Float,
    val spentAmounts: Map<String, Float> // Category -> Amount spent
)

class ViewBudgets : AppCompatActivity() {

    private lateinit var budgetsLayout: LinearLayout
    private lateinit var timePeriodSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_budgets)

        // Initialize views
        budgetsLayout = findViewById(R.id.budgetsLayout)
        timePeriodSpinner = findViewById(R.id.timePeriodSpinner)

        // Set up the Spinner for time periods
        val timePeriods = arrayOf("Day", "Week", "Month", "Year")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timePeriods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = adapter

        // Handle time period selection
        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriod = timePeriods[position]
                // Filter and update data based on selected period here (you can implement filtering logic)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        // Example vbBudgets (replace with real data from database)
        val vbBudgetsList = listOf(
            vbBudgets("Monthly Grocery Budget", 5000f, mapOf(
                "Groceries" to 1500f,
                "Transport" to 800f,
                "Entertainment" to 1200f,
                "Other" to 3000f // Example for overspending
            )),
            vbBudgets("Yearly Holiday Fund", 20000f, mapOf(
                "Flights" to 10000f,
                "Accommodation" to 6000f,
                "Transport" to 3000f // Example for overspending
            ))
        )

        // Display each budget
        vbBudgetsList.forEach { vbBudget ->
            displayBudget(vbBudget)
        }
    }

    private fun displayBudget(vbBudget: vbBudgets) {
        // Inflate the layout for each vbBudget
        val budgetView = layoutInflater.inflate(R.layout.budget_item, null)

        val txtBudgetName: TextView = budgetView.findViewById(R.id.txtBudgetName)
        val pieChart: PieChart = budgetView.findViewById(R.id.pieChart)
        val txtTotalBudget: TextView = budgetView.findViewById(R.id.txtTotalBudget)
        val txtSpent: TextView = budgetView.findViewById(R.id.txtSpent)
        val txtRemaining: TextView = budgetView.findViewById(R.id.txtRemaining)
        val txtWarning: TextView = budgetView.findViewById(R.id.txtWarning)
        val progressBar: ProgressBar = budgetView.findViewById(R.id.progressBar)

        // Set budget name and data
        txtBudgetName.text = vbBudget.name
        updateDashboard(vbBudget, pieChart, txtTotalBudget, txtSpent, txtRemaining, txtWarning, progressBar)

        // Add the budget view to the layout
        budgetsLayout.addView(budgetView)
    }

    private fun updateDashboard(
        vbBudget: vbBudgets,
        pieChart: PieChart,
        txtTotalBudget: TextView,
        txtSpent: TextView,
        txtRemaining: TextView,
        txtWarning: TextView,
        progressBar: ProgressBar
    ) {
        val totalSpent = vbBudget.spentAmounts.values.sum()
        val remainingBudget = vbBudget.totalBudget - totalSpent

        // Update text fields
        txtTotalBudget.text = "Total Budget: R${vbBudget.totalBudget}"
        txtSpent.text = "Spent: R$totalSpent"
        txtRemaining.text = "Remaining: R$remainingBudget"

        // Show warning if overspending
        txtWarning.apply {
            text = "⚠️ Overspending!"
            visibility = if (remainingBudget < 0) View.VISIBLE else View.GONE
        }

        // Update progress bar
        val progress = if (vbBudget.totalBudget > 0) {
            (totalSpent / vbBudget.totalBudget * 100).toInt()
        } else {
            0
        }
        progressBar.progress = progress

        // Create Pie Chart Entries
        val entries = ArrayList<PieEntry>()
        for ((category, amount) in vbBudget.spentAmounts) {
            entries.add(PieEntry(amount, category))
        }

        // Define custom ValueFormatter
        val valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "R${value}"
            }
        }

        // Set up Pie Chart
        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = listOf(Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA)
        dataSet.sliceSpace = 2f
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = valueFormatter // Set the custom ValueFormatter here

        val pieData = PieData(dataSet).apply {
            setDrawValues(true)
            setValueTextColor(Color.BLACK)
        }

        pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            this.data = pieData // Apply data correctly
            invalidate() // Refresh chart
        }
    }
}
