package Data_Classes

import androidx.room.Entity

@Entity(
    primaryKeys = ["budgetId", "categoryId"]
)
data class BudgetCategoryCrossRef(
    val budgetId: Int,
    val categoryId: Int
)
